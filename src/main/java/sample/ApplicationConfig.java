package sample;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.*;
import org.springframework.boot.actuate.health.Health.Builder;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.*;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import com.fasterxml.jackson.datatype.hibernate5.Hibernate5Module;

import sample.context.Timestamper;
import sample.model.BusinessDayHandler;

/**
 * アプリケーションにおけるBean定義を表現します。
 * <p>クラス側でコンポーネント定義していない時はこちらで明示的に記載してください。
 */
@Configuration
public class ApplicationConfig {

    /** SpringMvcの拡張コンフィギュレーション */
    @Configuration
    public static class WebMvcConfig extends WebMvcConfigurerAdapter {
        @Autowired
        private MessageSource message;

        /** HibernateのLazyLoading回避対応。  see JacksonAutoConfiguration */
        @Bean
        public Hibernate5Module jsonHibernate5Module() {
            return new Hibernate5Module();
        }

        /** BeanValidationメッセージのUTF-8に対応したValidator。 */
        @Bean
        public LocalValidatorFactoryBean validator() {
            LocalValidatorFactoryBean factory = new LocalValidatorFactoryBean();
            factory.setValidationMessageSource(message);
            return factory;
        }

        /** 標準Validatorの差し替えをします。 */
        @Override
        public org.springframework.validation.Validator getValidator() {
            return validator();
        }
    }

    /** 拡張ヘルスチェック定義を表現します。 */
    @Configuration
    public static class HealthCheckConfig {
        /** 営業日チェック */
        @Bean
        public HealthIndicator dayIndicator(final Timestamper time, final BusinessDayHandler day) {
            return new AbstractHealthIndicator() {
                @Override
                protected void doHealthCheck(Builder builder) throws Exception {
                    builder.up();
                    builder.withDetail("day", day.day())
                            .withDetail("dayMinus1", day.day(-1))
                            .withDetail("dayPlus1", day.day(1))
                            .withDetail("dayPlus2", day.day(2))
                            .withDetail("dayPlus3", day.day(3));
                }
            };
        }
    }

}
