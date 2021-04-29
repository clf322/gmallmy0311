package com.atguigu.gmallmy0311.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
@Configuration
public class WebMvcConfiguration extends WebMvcConfigurerAdapter{
    @Autowired
    AuthInterceptor authInterceptor;



   public void addInterceptors (InterceptorRegistry registry){
registry.addInterceptor(authInterceptor).addPathPatterns("/**");
   super.addInterceptors(registry);


   }

}
