package tacos.web.api;

import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;

import tacos.Taco;
import tacos.web.DesignTacoController;

public class TacoResourceAssembler extends RepresentationModelAssemblerSupport<Taco, TacoResource>{

	public TacoResourceAssembler() {
		super(DesignTacoController.class, TacoResource.class);
	}
	
	@Override
	protected TacoResource instantiateModel(Taco entity) {
		return new TacoResource(entity);
	}
	
	@Override
	public TacoResource toModel(Taco entity) {
		return createModelWithId(entity.getId(), entity);
	}
	
	
}
