package cn.cruder.dousx.rdl.annotation;


import cn.cruder.dousx.rdl.autoconfiguration.RdlConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigurationPackage;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * 使用了@Import注解,取代从/META-INF/spring.factories加载配置
 *
 * @author dousx
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@AutoConfigurationPackage
@EnableCaching
@EnableAspectJAutoProxy
@Import({RdlConfiguration.class})
public @interface EnableRdl {
}
