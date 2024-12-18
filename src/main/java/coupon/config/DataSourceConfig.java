package coupon.config;

import javax.sql.DataSource;
import java.util.Map;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy;

@Configuration
public class DataSourceConfig {

    @Bean
    @ConfigurationProperties(prefix = "coupon.datasource.writer")
    public DataSource writeDataSource() {
        return DataSourceBuilder.create().type(HikariDataSource.class).build();
    }

    @Bean
    @ConfigurationProperties(prefix = "coupon.datasource.reader")
    public DataSource readDataSource() {
        return DataSourceBuilder.create().type(HikariDataSource.class).build();
    }

    @Bean
    @DependsOn({"writeDataSource", "readDataSource"})
    public DataSource routeDataSource() {
        DataSourceRouter dataSourceRouter = new DataSourceRouter();
        DataSource writeDataSource = writeDataSource();
        DataSource readDataSource = readDataSource();

        Map<Object, Object> dataSourceMap = Map.of(
                "write", writeDataSource,
                "read", readDataSource
        );
        dataSourceRouter.setTargetDataSources(dataSourceMap);
        dataSourceRouter.setDefaultTargetDataSource(writeDataSource);

        return dataSourceRouter;
    }

    @Bean
    @Primary
    @DependsOn("routeDataSource")
    public DataSource dataSource() {
        return new LazyConnectionDataSourceProxy(routeDataSource());
    }
}
