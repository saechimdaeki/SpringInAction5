package tacos.web.api;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import tacos.Taco;
import tacos.data.TacoRepository;

import java.util.List;

@RepositoryRestController
public class RecentTacosController {

    private TacoRepository tacoRepo;

    public RecentTacosController(TacoRepository tacoRepo){
        this.tacoRepo=tacoRepo;
    }

    @GetMapping(path = "/tacos/recent",produces = "application/hal+json")
    public ResponseEntity<CollectionModel<TacoResource>> recentTacos(){
        PageRequest page=PageRequest.of(0,12, Sort.by("createdAt").descending());

        List<Taco> tacos=tacoRepo.findAll(page).getContent();

        CollectionModel<TacoResource> tacoResources=  new TacoResourceAssembler().toCollectionModel(tacos);

        CollectionModel<TacoResource> recentResources=new CollectionModel<>(tacoResources);

        recentResources.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(RecentTacosController.class).recentTacos())
        .withRel("recents"));
        return new ResponseEntity<>(recentResources, HttpStatus.OK);
    }
}
