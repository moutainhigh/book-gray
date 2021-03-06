package cn.dingyuegroup.gray.client;

import cn.dingyuegroup.gray.client.config.GrayClientMarkerConfiguration;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Import(GrayClientMarkerConfiguration.class)
@EnableFeignClients(basePackages = {"cn.dingyuegroup.gray"})
public @interface EnableGrayClient {

}
