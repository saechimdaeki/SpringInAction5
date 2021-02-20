package tacos.web.api;

import org.springframework.hateoas.RepresentationModel;

import lombok.Getter;
import tacos.Ingredient;
import tacos.Ingredient.Type;

public class IngredientResource extends RepresentationModel<IngredientResource> {

  @Getter
  private String name;

  @Getter
  private Type type;

  public IngredientResource(Ingredient ingredient){
    this.name=ingredient.getName();
    this.type=ingredient.getType();
  }
}
