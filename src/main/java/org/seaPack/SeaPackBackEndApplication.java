package org.seaPack;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * SeaPack 后端应用入口
 * <p>启用定时任务调度以支持股息率监控等后台任务。</p>
 */
@SpringBootApplication
@EnableScheduling
@MapperScan("org.seaPack.mapper")
public class SeaPackBackEndApplication {

	public static void main(String[] args) {
		SpringApplication.run(SeaPackBackEndApplication.class, args);
	}

}
