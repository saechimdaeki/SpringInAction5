package tacos.web;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.EntityLinks;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;


import tacos.Taco;
import tacos.data.TacoRepository;
import tacos.web.api.TacoResource;
import tacos.web.api.TacoResourceAssembler;


@RequestMapping(path="design", produces = "application/json")
@CrossOrigin(origins = "*")
public class DesignTacoController {
	
	private TacoRepository tacoRepo;
	
	@Autowired
	EntityLinks entityLinks;
	
	public DesignTacoController(TacoRepository tacoRepo) {
		this.tacoRepo=tacoRepo;
	}
	
	@GetMapping("/recent")
	public Iterable<Taco> recentTacos(){
		PageRequest page=PageRequest.of(0, 12,Sort.by("createdAt").descending());
		
		return tacoRepo.findAll(page).getContent();
	}

// 책과 다른최신버젼의 코
//	@GetMapping("/recent")
//	public CollectionModel<EntityModel<Taco>> recentTacos(){
//		PageRequest page=PageRequest.of(0, 12,Sort.by("createdAt").descending());
//		
//		List<Taco> tacos=tacoRepo.findAll(page).getContent();
//		
//		List<TacoResource> tacoResources= new TacoResourceAssembler().toModel(tacos);
//		CollectionModel<TacoResource> recentResources= new CollectionModel<TacoResource>(tacoResources);
//	recentResources.add(
//			WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(DesignTacoController.class).recentTacos())
//			.withRel("recents")
//			);
//	}
	
	
	
	@GetMapping("/{id}")
	public ResponseEntity<Taco> tacoById(@PathVariable("id")Long id){
		Optional<Taco> optTaco=tacoRepo.findById(id);
		if(optTaco.isPresent()) {
			return new ResponseEntity<>(optTaco.get(),HttpStatus.OK);
		}
		return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
	}
	
	@PostMapping(consumes = "application/json")
	@ResponseStatus(HttpStatus.CREATED)
	public Taco postTaco(@RequestBody Taco taco) {
		return tacoRepo.save(taco);
	}
	

}
