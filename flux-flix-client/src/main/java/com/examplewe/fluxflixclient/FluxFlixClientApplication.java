package com.examplewe.fluxflixclient;

import java.util.Date;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.client.WebClient;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;


@SpringBootApplication
public class FluxFlixClientApplication {
	
	
	@Bean 
	WebClient client(){
		 return WebClient.create();
	}
	
	@Bean
	CommandLineRunner demo(WebClient client) {
			
		 return args->{
			 client.get().uri("http://localhost:8080/movies")
				.retrieve()
				.bodyToFlux(Movie.class)
				.filter(movie -> movie.getTitle().equals("Harry potter"))
				.flatMap(movie->
				client.get().uri("http://localhost:8080/movies/{id}/events", movie.getId())
						.retrieve()
						.bodyToFlux(MovieEvent.class))
						.subscribe(System.out::println);
		};
	}

	public static void main(String[] args) {
		SpringApplication.run(FluxFlixClientApplication.class, args);
	}
}
@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
class MovieEvent {

	private Movie movie;
	private Date when;
	private String user;

}
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
class Movie {
	

	private String id,title, genre;


}