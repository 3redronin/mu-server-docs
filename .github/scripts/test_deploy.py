import contextlib
import hashlib
import hmac
import io
import json
import os
import unittest
from unittest.mock import Mock, patch

import deploy


class DeploymentTest(unittest.TestCase):
    def setUp(self):
        self.config = {"webhook_url": "https://example.com/webhook", "webhook_secret": "test-only-" * 8,
                       "branch": "release/test"}
        self.sha = "a" * 40
        self.environ = {"COOLIFY_DEPLOY_CONFIG": json.dumps(self.config), "GITHUB_EVENT_NAME": "push",
                        "GITHUB_REF": "refs/heads/release/test", "GITHUB_SHA": self.sha,
                        "GITHUB_REPOSITORY": "example/docs", "GH_TOKEN": "test-only-api-token"}

    def run_main(self, responses):
        output = io.StringIO()
        with patch.dict(os.environ, self.environ, clear=True), \
                patch.object(deploy.urllib.request, "build_opener", return_value=Mock()), \
                patch.object(deploy, "request_json", side_effect=responses) as request, \
                contextlib.redirect_stdout(output):
            deploy.main()
        return request, output.getvalue()

    def test_pr_cannot_trigger_deployment(self):
        self.environ["GITHUB_EVENT_NAME"] = "pull_request"
        request, _ = self.run_main([])
        request.assert_not_called()

    def test_other_branch_cannot_trigger_deployment(self):
        self.environ["GITHUB_REF"] = "refs/heads/other"
        request, _ = self.run_main([])
        request.assert_not_called()

    def test_superseded_commit_is_not_deployed(self):
        request, _ = self.run_main([{"object": {"sha": "b" * 40}}])
        self.assertEqual(request.call_count, 1)

    def test_only_exact_tested_commit_is_signed_without_forwarding_api_token(self):
        request, output = self.run_main([{"object": {"sha": self.sha}}, [{"status": "queued"}]])
        sent = request.call_args_list[1].args[1]
        body = json.loads(sent.data)
        self.assertEqual(body["after"], self.sha)
        self.assertEqual(body["ref"], self.environ["GITHUB_REF"])
        expected = "sha256=" + hmac.new(self.config["webhook_secret"].encode(), sent.data, hashlib.sha256).hexdigest()
        self.assertEqual(sent.get_header("X-hub-signature-256"), expected)
        self.assertIsNone(sent.get_header("Authorization"))
        self.assertNotIn(self.config["webhook_secret"], output)
        self.assertNotIn(self.config["webhook_url"], output)

    def test_http_success_with_rejected_deployment_is_failure(self):
        with self.assertRaises(ValueError):
            self.run_main([{"object": {"sha": self.sha}}, [{"status": "failed"}]])

    def test_redirects_are_not_followed(self):
        self.assertIsNone(deploy.NoRedirect().redirect_request(None, None, 302, "", {}, "https://example.org"))


if __name__ == "__main__":
    unittest.main()
