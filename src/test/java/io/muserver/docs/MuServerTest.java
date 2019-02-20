package io.muserver.docs;

import io.muserver.MuServer;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;


public class MuServerTest {

    @Test
    public void versionIsAvailable() {
        assertThat(MuServer.artifactVersion(), not(containsString(".x")));
    }
}
