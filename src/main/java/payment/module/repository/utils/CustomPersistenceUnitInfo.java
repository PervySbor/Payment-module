package payment.module.repository.utils;

import jakarta.persistence.SharedCacheMode;
import jakarta.persistence.ValidationMode;
import jakarta.persistence.spi.ClassTransformer;
import jakarta.persistence.spi.PersistenceUnitInfo;
import jakarta.persistence.spi.PersistenceUnitTransactionType;

import javax.sql.DataSource;
import java.net.URL;
import java.util.List;
import java.util.Properties;

//throws error & fails in case it can't read config.json
//(but sends logs)
public class CustomPersistenceUnitInfo implements PersistenceUnitInfo {

    @Override
    public String getPersistenceUnitName() {
        return "hibernate.postgres";
    }

    @Override
    public String getPersistenceProviderClassName() {
        return "org.hibernate.jpa.HibernatePersistenceProvider";
    }

    @Override
    public PersistenceUnitTransactionType getTransactionType() {
        return PersistenceUnitTransactionType.RESOURCE_LOCAL;
    }

    @Override
    public DataSource getJtaDataSource() {
        return MyHikariDataSource.getDataSource();
    }

    @Override
    public DataSource getNonJtaDataSource() {
        return null;
    }

    @Override
    public List<String> getMappingFileNames() {
        return List.of();
    }

    @Override
    public List<URL> getJarFileUrls() {
        return List.of();
    }

    @Override
    public URL getPersistenceUnitRootUrl() {
        return null;
    }

    @Override
    public List<String> getManagedClassNames() {
        return List.of("payment.module.repository.entities.Payment");
    }

    @Override
    public boolean excludeUnlistedClasses() {
        return false;
    }

    @Override
    public SharedCacheMode getSharedCacheMode() {
        return null;
    }

    @Override
    public ValidationMode getValidationMode() {
        return null;
    }

    @Override
    public Properties getProperties() {
//        Properties properties = new Properties();
//        String DB_URL = ConfigReader.getStringValue("DB_URL");
//        String DB_LOGIN = ConfigReader.getStringValue("DB_LOGIN");
//        String DB_PASSWORD = ConfigReader.getStringValue("DB_PASSWORD");
//
//        System.out.println(DB_URL);
//        System.out.println(DB_LOGIN);
//        System.out.println(DB_PASSWORD);
//
//        properties.put("hibernate.connection.driver_class", "org.postgresql.Driver");
//        properties.put("hibernate.connection.url", DB_URL);
//        properties.put("hibernate.connection.username", DB_LOGIN);
//        properties.put("hibernate.connection.password", DB_PASSWORD);
//        properties.put("hibernate.show_sql", "true");
//
//        return properties;
        return null;
    }

    @Override
    public String getPersistenceXMLSchemaVersion() {
        return "";
    }

    @Override
    public ClassLoader getClassLoader() {
        return null;
    }

    @Override
    public void addTransformer(ClassTransformer classTransformer) {

    }

    @Override
    public ClassLoader getNewTempClassLoader() {
        return null;
    }
}
