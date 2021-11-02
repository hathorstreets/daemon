package com.hathor.daemon;

import com.hathor.daemon.services.DaemonService;
import com.hathor.daemon.services.dto.Balance;
import com.hathor.daemon.services.dto.Response;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SpringBootTest
class HathorStreetsApplicationTests {
	Logger logger = LoggerFactory.getLogger(HathorStreetsApplicationTests.class);

	@Test
	void contextLoads() {
	}

	@Test
	void addressesTest() {
		List<Thread> threads = new ArrayList<>();

		ExecutorService es = Executors.newFixedThreadPool(10);

		for (int i = 0; i < 100; i++) {
			es.submit(new Runnable() {
				@Override
				public void run() {
					try {
						String mint = addressEndpoint();

						if (mint.isEmpty()) {
							logger.error(mint);
						}

						logger.debug(mint);
					}
					catch(Exception ex) {
						ex.printStackTrace();
					}
				}
			});
			try {
				Thread.sleep(100);
			} catch (Exception ex) {}
		}
		es.shutdown();
	}

	public static String addressEndpoint() throws IOException
	{
		URL website = new URL("http://localhost:5000/mint/HSHqkJstGEDZN5m5AKXvX9d1j56DwATNSG/1");
		URLConnection connection = website.openConnection();
		BufferedReader in = new BufferedReader(
				new InputStreamReader(
						connection.getInputStream()));

		StringBuilder response = new StringBuilder();
		String inputLine;

		while ((inputLine = in.readLine()) != null)
			response.append(inputLine);

		in.close();

		return response.toString();
	}

	public void safePrintln(String s) {
		synchronized (System.out) {
			System.out.println(s);
		}
	}
}
