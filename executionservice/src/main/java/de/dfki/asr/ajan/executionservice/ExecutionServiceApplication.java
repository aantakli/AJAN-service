/*
 * Copyright (C) 2020 see AJAN-service/AUTHORS.txt (German Research Center for Artificial Intelligence, DFKI).
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */

package de.dfki.asr.ajan.executionservice;

import io.swagger.jaxrs.config.BeanConfig;
import io.swagger.jaxrs.listing.ApiListingResource;
import io.swagger.jaxrs.listing.SwaggerSerializers;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
@Import({ApiListingResource.class, SwaggerSerializers.class})
@ApplicationPath(ExecutionServiceApplication.BASE_PATH)
public class ExecutionServiceApplication extends Application {
	public static final String BASE_PATH = "/ajan";

	@Bean
	public BeanConfig swaggerConfig() {
		BeanConfig beanConfig = new BeanConfig();
		beanConfig.setVersion("1.0");
		beanConfig.setBasePath(BASE_PATH);
		beanConfig.setResourcePackage("de.dfki.asr.ajan.rest");
		beanConfig.setScan(true);
		return beanConfig;
	}
        
        @Bean
        public CorsFilter corsFilter() {
               UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
               CorsConfiguration config = new CorsConfiguration();
               config.addAllowedOrigin("*");
               config.addAllowedHeader("*");
               config.addAllowedMethod("OPTIONS");
               config.addAllowedMethod("HEAD");
               config.addAllowedMethod("GET");
               config.addAllowedMethod("PUT");
               config.addAllowedMethod("POST");
               config.addAllowedMethod("DELETE");
               config.addAllowedMethod("PATCH");
               source.registerCorsConfiguration("/**", config);
               return new CorsFilter(source);
        }
}
