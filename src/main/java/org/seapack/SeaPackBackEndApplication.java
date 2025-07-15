package org.seapack;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("org.seapack.mapper")
public class SeaPackBackEndApplication {

	public static void main(String[] args) {
		SpringApplication.run(SeaPackBackEndApplication.class, args);
	}

}
