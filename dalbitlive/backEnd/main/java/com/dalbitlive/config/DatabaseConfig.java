package com.dalbitlive.config;

import com.dalbitlive.mybatis.RefreshableSqlSessionFactoryBean;
import com.dalbitlive.mybatis.ReplicationRoutingDataSource;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.var;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.HashMap;

@Configuration
@EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class})
@EnableTransactionManagement
@MapperScan(basePackages= "com.dalbitlive")
public class DatabaseConfig {

    @Value("${spring.datasource.driverClassName}")
    private String JDBC_DRIVER_CLASS_NAME;

    @Value("${spring.datasource.master.url}")
    private String JDBC_MASTER_URL;

    @Value("${spring.datasource.slave.url}")
    private String JDBC_SLAVE_URL;

    @Value("${spring.datasource.username}")
    private String JDBC_USERNAME;

    @Value("${spring.datasource.password}")
    private String JDBC_PASSWORD;

    @Value("${spring.datasource.connection.timeout}")
    private long CONNECTION_TIMEOUT;

    @Value("${spring.datasource.max.lifetime}")
    private long MAX_LIFETIME;

    @Value("${spring.datasource.hikari.idle-timeout}")
    private long IDLE_TIMEOUT;

    @Bean
    public DataSource masterDataSource() {

        HikariConfig masterHikariConfig = new HikariConfig();
        masterHikariConfig.setDriverClassName(JDBC_DRIVER_CLASS_NAME);
        masterHikariConfig.setJdbcUrl(JDBC_MASTER_URL);
        masterHikariConfig.setUsername(JDBC_USERNAME);
        masterHikariConfig.setPassword(JDBC_PASSWORD);
        //masterHikariConfig.addDataSourceProperty("autoReconnect",true);
        masterHikariConfig.addDataSourceProperty("tcpKeepAlive", true);
        masterHikariConfig.setMaximumPoolSize(10);
        masterHikariConfig.setMinimumIdle(6);
        masterHikariConfig.setConnectionTimeout(CONNECTION_TIMEOUT);
        masterHikariConfig.setIdleTimeout(IDLE_TIMEOUT);
        masterHikariConfig.setMaxLifetime(MAX_LIFETIME);
        masterHikariConfig.setAutoCommit(true);

        return new HikariDataSource(masterHikariConfig);
    }

    @Bean
    public DataSource slaveDataSource() {
        HikariConfig slaveHikariConfig = new HikariConfig();
        slaveHikariConfig.setDriverClassName(JDBC_DRIVER_CLASS_NAME);
        slaveHikariConfig.setJdbcUrl(JDBC_SLAVE_URL);
        slaveHikariConfig.setUsername(JDBC_USERNAME);
        slaveHikariConfig.setPassword(JDBC_PASSWORD);
        //slaveHikariConfig.addDataSourceProperty("autoReconnect",true);
        slaveHikariConfig.addDataSourceProperty("tcpKeepAlive", true);
        slaveHikariConfig.setMaximumPoolSize(10);
        slaveHikariConfig.setMinimumIdle(6);
        slaveHikariConfig.setConnectionTimeout(CONNECTION_TIMEOUT);
        slaveHikariConfig.setIdleTimeout(IDLE_TIMEOUT);
        slaveHikariConfig.setMaxLifetime(MAX_LIFETIME);
        slaveHikariConfig.setAutoCommit(true);

        return new HikariDataSource(slaveHikariConfig);
    }

    @Bean
    public DataSource routingDataSource(@Qualifier("masterDataSource") DataSource masterDataSource,
                                        @Qualifier("slaveDataSource") DataSource slaveDataSource) {

        var dataSourceMap = new HashMap<>();
        dataSourceMap.put("master", masterDataSource);
        dataSourceMap.put("slave", slaveDataSource);

        var routingDataSource = new ReplicationRoutingDataSource();
        routingDataSource.setTargetDataSources(dataSourceMap);
        routingDataSource.setDefaultTargetDataSource(masterDataSource);

        return routingDataSource;
    }

    @Primary
    @Bean
    public DataSource dataSource(@Qualifier("routingDataSource") DataSource routingDataSource) {
        return new LazyConnectionDataSourceProxy(routingDataSource);
    }

    @Bean
    public SqlSessionFactory sqlSessionFactory(DataSource dataSource) throws Exception {

        RefreshableSqlSessionFactoryBean refreshableSqlSessionFactoryBean = new RefreshableSqlSessionFactoryBean();
        refreshableSqlSessionFactoryBean.setDataSource(dataSource);
        refreshableSqlSessionFactoryBean.setMapperLocations(new PathMatchingResourcePatternResolver().getResources("classpath*:com/dalbitlive/**/mapper/**/*.xml"));
        refreshableSqlSessionFactoryBean.setConfigLocation(new DefaultResourceLoader().getResource("classpath:mybatis/mybatis-config.xml"));
        refreshableSqlSessionFactoryBean.afterPropertiesSet();

        return refreshableSqlSessionFactoryBean.getObject();
    }

    @Bean
    public SqlSessionTemplate sqlSessionTemplate(SqlSessionFactory sqlSessionFactory) throws Exception {
        final SqlSessionTemplate sqlSessionTemplate = new SqlSessionTemplate(sqlSessionFactory);
        return sqlSessionTemplate;
    }

    @Bean
    public PlatformTransactionManager transactionManager(DataSource dataSource){
        return new DataSourceTransactionManager(dataSource);
    }
}
