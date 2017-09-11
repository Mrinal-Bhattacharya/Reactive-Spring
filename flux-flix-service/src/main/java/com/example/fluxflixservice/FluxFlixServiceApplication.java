package com.example.fluxflixservice;

import static org.springframework.web.reactive.function.server.ServerResponse.ok;

import java.time.Duration;
import java.util.Date;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Stream;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

@SpringBootApplication
public class FluxFlixServiceApplication {

	@Bean
	RouterFunction<ServerResponse> routers(FluxFlixService service) {
		return RouterFunctions.route(RequestPredicates.GET("/movies"), request -> ok().body(service.all(), Movie.class))
				.andRoute(RequestPredicates.GET("/movies/{id}"),
						request -> ok().body(service.byId(request.pathVariable("id")), Movie.class))
				.andRoute(RequestPredicates.GET("/movies/{id}/events"),
						request -> ok().contentType(MediaType.TEXT_EVENT_STREAM).body(
								service.byId(request.pathVariable("id")).flatMapMany(service::streamStreams),
								MovieEvent.class));
	}

	public static void main(String[] args) {
		SpringApplication.run(FluxFlixServiceApplication.class, args);
	}

	@Bean
	CommandLineRunner demo(MovieRespoitory movieRespoitory) {
		return args -> {
			movieRespoitory.deleteAll().subscribe(null, null,
					() -> Stream.of("Harry potter", "Attack of the fluxxed", "Back to the future")
							.map(name -> new Movie(UUID.randomUUID().toString(), name, randonGenere()))
							.forEach(m -> movieRespoitory.save(m).subscribe(System.out::println)));
		};
	}

	private String randonGenere() {
		String[] generes = "horror,drama,action".split(",");
		return generes[new Random().nextInt(generes.length)];
	}
}

// https://www.youtube.com/watch?v=0pD7YeTAUkk
// https://www.youtube.com/watch?v=zVNIZXf4BG8
// https://www.youtube.com/watch?v=EoK5a99Bmjc
// https://www.youtube.com/results?q=Spring+boot+functional&sp=UBQ%253D
@Data
@NoArgsConstructor
@AllArgsConstructor
class MovieEvent {

	private Movie movie;
	private Date when;
	private String user;

}

@Service
class FluxFlixService {

	private final MovieRespoitory movieRespoitory;

	public FluxFlixService(MovieRespoitory movieRespoitory) {
		this.movieRespoitory = movieRespoitory;
	}

	public Flux<MovieEvent> streamStreams(Movie movie) {
		Flux<Long> interval = Flux.interval(Duration.ofSeconds(1));
		Flux<MovieEvent> events = Flux
				.fromStream(Stream.generate(() -> new MovieEvent(movie, new Date(), randonUser())));
		return Flux.zip(interval, events).map(Tuple2::getT2);

	}

	public Flux<Movie> all() {
		return movieRespoitory.findAll();
	}

	public Mono<Movie> byId(String id) {
		return movieRespoitory.findById(id);
	}

	private String randonUser() {
		String[] users = "Phill,Mrinal,Saurabh".split(",");
		return users[new Random().nextInt(users.length)];
	}
}

interface MovieRespoitory extends ReactiveMongoRepository<Movie, String> {

}

/*@RestController
@RequestMapping("/movies")
class MovieRestController {

	private FluxFlixService fluxFlixService;

	public MovieRestController(FluxFlixService fluxFlixService) {
		this.fluxFlixService = fluxFlixService;
	}

	@GetMapping(value = "/{id}/events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	public Flux<MovieEvent> events(@PathVariable String id) {

		Mono<Movie> byId = fluxFlixService.byId(id);
		return byId.flatMapMany(fluxFlixService::streamStreams);
		// return fluxFlixService.streamStreams(byId);
	}

	@GetMapping
	public Flux<Movie> all() {
		return fluxFlixService.all();
	}

	@GetMapping("{id}")
	public Mono<Movie> byId(@PathVariable String id) {
		return fluxFlixService.byId(id);
	}

}*/

@Document
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
class Movie {
	

	@Id
	private String id;
	private String title, genre;


}