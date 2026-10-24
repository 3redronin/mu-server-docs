"""Send a signed deployment event for the tested push; keep connection details private."""
import hashlib
import hmac
import json
import os
import re
import sys
import urllib.error
import urllib.parse
import urllib.request
import uuid


def configuration(environ):
    try:
        config = json.loads(environ.get("COOLIFY_DEPLOY_CONFIG", ""))
        url = urllib.parse.urlsplit(config["webhook_url"])
        secret = config["webhook_secret"]
        branch = config["branch"]
        if (url.scheme != "https" or not url.hostname or url.username or url.password
                or url.fragment or url.query or not isinstance(secret, str) or len(secret) < 32
                or not isinstance(branch, str) or not branch):
            raise ValueError()
        return config
    except (KeyError, TypeError, ValueError):
        raise ValueError("Configure the COOLIFY_DEPLOY_CONFIG repository Actions secret.") from None


def payload_for(environ, branch):
    repository = environ.get("GITHUB_REPOSITORY", "")
    sha = environ.get("GITHUB_SHA", "")
    ref = environ.get("GITHUB_REF", "")
    if environ.get("GITHUB_EVENT_NAME") != "push" or ref != "refs/heads/" + branch:
        return None
    if not re.fullmatch(r"[A-Za-z0-9_.-]+/[A-Za-z0-9_.-]+", repository):
        raise ValueError("Invalid repository metadata.")
    if not re.fullmatch(r"[0-9a-f]{40}", sha):
        raise ValueError("Invalid commit metadata.")
    return {"ref": ref, "after": sha, "repository": {"full_name": repository}}


def request_json(opener, request):
    # Neither response bodies nor connection errors are logged: they can contain private details.
    with opener.open(request, timeout=30) as response:
        return json.loads(response.read(1024 * 1024))


class NoRedirect(urllib.request.HTTPRedirectHandler):
    def redirect_request(self, *args, **kwargs):
        return None


def main():
    config = configuration(os.environ)
    payload = payload_for(os.environ, config["branch"])
    if payload is None:
        print("This branch has no configured deployment target.")
        return

    opener = urllib.request.build_opener(NoRedirect())
    branch_path = urllib.parse.quote(config["branch"], safe="")
    latest = request_json(opener, urllib.request.Request(
        "https://api.github.com/repos/" + payload["repository"]["full_name"] + "/git/ref/heads/" + branch_path,
        headers={"Authorization": "Bearer " + os.environ["GH_TOKEN"],
                 "Accept": "application/vnd.github+json"}))
    if latest["object"]["sha"] != payload["after"]:
        print("A newer commit exists; skipping this superseded deployment.")
        return

    body = json.dumps(payload, separators=(",", ":")).encode()
    signature = hmac.new(config["webhook_secret"].encode(), body, hashlib.sha256).hexdigest()
    result = request_json(opener, urllib.request.Request(config["webhook_url"], data=body, headers={
        "Content-Type": "application/json", "X-GitHub-Event": "push",
        "X-GitHub-Delivery": str(uuid.uuid4()), "X-Hub-Signature-256": "sha256=" + signature,
    }))
    if not isinstance(result, list) or not result or any(
            not isinstance(row, dict) or row.get("status") != "queued" for row in result):
        raise ValueError("Deployment was not queued; check the private deployment dashboard.")
    print("Deployment queued for tested commit " + payload["after"] + ".")


if __name__ == "__main__":
    try:
        main()
    except (ValueError, KeyError, TypeError, OSError, urllib.error.URLError):
        sys.exit("Deployment trigger failed. Check repository secrets and the private deployment dashboard.")
