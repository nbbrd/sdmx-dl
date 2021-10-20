package internal.util.rest;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatNullPointerException;

public class HttpRestTest {

    @Test
    public void testFactory() {
        assertThatNullPointerException()
                .isThrownBy(() -> HttpRest.newClient(null));
    }
}
