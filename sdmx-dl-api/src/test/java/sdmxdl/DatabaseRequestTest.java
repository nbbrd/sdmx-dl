package sdmxdl;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Philippe Charles
 */
public class DatabaseRequestTest {

    @Test
    public void testDefaults() {
        DatabaseRequest request = DatabaseRequest.builder().build();

        assertThat(request.getDatabase()).isEqualTo(DatabaseRef.NO_DATABASE);
        assertThat(request.getLanguages()).isEqualTo(Languages.ANY);
    }

    @Test
    public void testBuilderConvenience() {
        DatabaseRequest request = DatabaseRequest.builder()
                .databaseOf("db")
                .languagesOf("fr")
                .build();

        assertThat(request.getDatabase()).isEqualTo(DatabaseRef.parse("db"));
        assertThat(request.getLanguages()).isEqualTo(Languages.parse("fr"));
    }

    @Test
    public void testBuilderOf() {
        SourceRequest source = SourceRequest.builder().languagesOf("fr").build();

        DatabaseRequest request = DatabaseRequest.builderOf(source).build();

        assertThat(request.getLanguages()).isEqualTo(source.getLanguages());
        assertThat(request.getDatabase()).isEqualTo(DatabaseRef.NO_DATABASE);
    }
}

