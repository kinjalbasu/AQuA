#include 'CommonRules.lp'.
#include 'question.lp'.
#include 'Rules.lp'.
#include 'semanticRelations.lp'.
_is(apple, banana).
_mod(apple, 4).
_mod(banana, 5).
_property(1, banana, on, table).
apple(apple).
apple(banana).
banana(banana).
event(1, be, apple, null).
event(1, be, banana, null).
number(five).
number(four).
table(table).
abstract_entity(X, noun_tops) :- abstract_entity(X),not -abstract_entity(X, noun_tops).
abstract_entity(X, noun_tops) :- abstract_entity(X),properties_tops(X),not -abstract_entity(X, noun_tops).
abstract_entity(X, noun_tops) :- abstraction(X, noun_tops).
abstraction(X, noun_tops) :- abstraction(X),not -abstraction(X, noun_tops).
abstraction(X, noun_tops) :- abstraction(X),properties_tops(X),not -abstraction(X, noun_tops).
abstraction(X, noun_tops) :- group(X, noun_tops).
angiospermous_tree(X, noun_plant) :- angiospermous_tree(X),not -angiospermous_tree(X, noun_plant).
angiospermous_tree(X, noun_plant) :- angiospermous_tree(X),properties_plant(X),not -angiospermous_tree(X, noun_plant).
angiospermous_tree(X, noun_plant) :- fruit_tree(X, noun_plant).
apple(X, noun_food) :- apple(X),not -apple(X, noun_food),not apple(X, noun_plant).
apple(X, noun_food) :- apple(X),properties_food(X),not -apple(X, noun_food).
apple(X, noun_plant) :- apple(X),not -apple(X, noun_plant),-apple(X, noun_food).
apple(X, noun_plant) :- apple(X),properties_plant(X),not -apple(X, noun_plant).
apple_tree(X, noun_plant) :- apple(X, noun_plant).
apple_tree(X, noun_plant) :- apple_tree(X),not -apple_tree(X, noun_plant).
apple_tree(X, noun_plant) :- apple_tree(X),properties_plant(X),not -apple_tree(X, noun_plant).
arrangement(X, noun_group) :- arrangement(X),not -arrangement(X, noun_group).
arrangement(X, noun_group) :- arrangement(X),properties_group(X),not -arrangement(X, noun_group).
arrangement(X, noun_group) :- array(X, noun_group).
array(X, noun_group) :- array(X),not -array(X, noun_group).
array(X, noun_group) :- array(X),properties_group(X),not -array(X, noun_group).
array(X, noun_group) :- table(X, noun_group).
artifact(X, noun_tops) :- artifact(X),not -artifact(X, noun_tops).
artifact(X, noun_tops) :- artifact(X),properties_tops(X),not -artifact(X, noun_tops).
artifact(X, noun_tops) :- instrumentality(X, noun_artifact).
banana(X, noun_food) :- banana(X),not -banana(X, noun_food),-banana(X, noun_plant).
banana(X, noun_food) :- banana(X),properties_food(X),not -banana(X, noun_food).
banana(X, noun_plant) :- banana(X),not -banana(X, noun_plant),not banana(X, noun_food).
banana(X, noun_plant) :- banana(X),properties_plant(X),not -banana(X, noun_plant).
edible_fruit(X, noun_food) :- apple(X, noun_food).
edible_fruit(X, noun_food) :- banana(X, noun_food).
edible_fruit(X, noun_food) :- edible_fruit(X),not -edible_fruit(X, noun_food).
edible_fruit(X, noun_food) :- edible_fruit(X),properties_food(X),not -edible_fruit(X, noun_food).
entity(X, noun_tops) :- abstract_entity(X, noun_tops).
entity(X, noun_tops) :- entity(X),not -entity(X, noun_tops).
entity(X, noun_tops) :- entity(X),properties_tops(X),not -entity(X, noun_tops).
entity(X, noun_tops) :- physical_entity(X, noun_tops).
fare(X, noun_food) :- fare(X),not -fare(X, noun_food).
fare(X, noun_food) :- fare(X),properties_food(X),not -fare(X, noun_food).
fare(X, noun_food) :- table(X, noun_food).
food(X, noun_food) :- food(X),not -food(X, noun_food),-food(X, noun_tops).
food(X, noun_food) :- food(X),properties_food(X),not -food(X, noun_food).
food(X, noun_food) :- produce(X, noun_food).
food(X, noun_tops) :- fare(X, noun_food).
food(X, noun_tops) :- food(X),not -food(X, noun_tops),not food(X, noun_food).
food(X, noun_tops) :- food(X),properties_tops(X),not -food(X, noun_tops).
fruit_tree(X, noun_plant) :- apple_tree(X, noun_plant).
fruit_tree(X, noun_plant) :- fruit_tree(X),not -fruit_tree(X, noun_plant).
fruit_tree(X, noun_plant) :- fruit_tree(X),properties_plant(X),not -fruit_tree(X, noun_plant).
furnishing(X, noun_artifact) :- furnishing(X),not -furnishing(X, noun_artifact).
furnishing(X, noun_artifact) :- furnishing(X),properties_artifact(X),not -furnishing(X, noun_artifact).
furnishing(X, noun_artifact) :- furniture(X, noun_artifact).
furniture(X, noun_artifact) :- furniture(X),not -furniture(X, noun_artifact).
furniture(X, noun_artifact) :- furniture(X),properties_artifact(X),not -furniture(X, noun_artifact).
furniture(X, noun_artifact) :- table(X, noun_artifact).
gathering(X, noun_group) :- gathering(X),not -gathering(X, noun_group).
gathering(X, noun_group) :- gathering(X),properties_group(X),not -gathering(X, noun_group).
gathering(X, noun_group) :- table(X, noun_group).
geological_formation(X, noun_object) :- geological_formation(X),not -geological_formation(X, noun_object).
geological_formation(X, noun_object) :- geological_formation(X),properties_object(X),not -geological_formation(X, noun_object).
geological_formation(X, noun_object) :- natural_elevation(X, noun_object).
group(X, noun_tops) :- arrangement(X, noun_group).
group(X, noun_tops) :- group(X),not -group(X, noun_tops).
group(X, noun_tops) :- group(X),properties_tops(X),not -group(X, noun_tops).
group(X, noun_tops) :- social_group(X, noun_group).
herb(X, noun_plant) :- banana(X, noun_plant).
herb(X, noun_plant) :- herb(X),not -herb(X, noun_plant).
herb(X, noun_plant) :- herb(X),properties_plant(X),not -herb(X, noun_plant).
highland(X, noun_object) :- highland(X),not -highland(X, noun_object).
highland(X, noun_object) :- highland(X),properties_object(X),not -highland(X, noun_object).
highland(X, noun_object) :- tableland(X, noun_object).
instrumentality(X, noun_artifact) :- furnishing(X, noun_artifact).
instrumentality(X, noun_artifact) :- instrumentality(X),not -instrumentality(X, noun_artifact).
instrumentality(X, noun_artifact) :- instrumentality(X),properties_artifact(X),not -instrumentality(X, noun_artifact).
living_thing(X, noun_tops) :- living_thing(X),not -living_thing(X, noun_tops).
living_thing(X, noun_tops) :- living_thing(X),properties_tops(X),not -living_thing(X, noun_tops).
living_thing(X, noun_tops) :- organism(X, noun_tops).
natural_elevation(X, noun_object) :- highland(X, noun_object).
natural_elevation(X, noun_object) :- natural_elevation(X),not -natural_elevation(X, noun_object).
natural_elevation(X, noun_object) :- natural_elevation(X),properties_object(X),not -natural_elevation(X, noun_object).
object(X, noun_tops) :- geological_formation(X, noun_object).
object(X, noun_tops) :- living_thing(X, noun_tops).
object(X, noun_tops) :- object(X),not -object(X, noun_tops).
object(X, noun_tops) :- object(X),properties_tops(X),not -object(X, noun_tops).
object(X, noun_tops) :- whole(X, noun_tops).
organism(X, noun_tops) :- organism(X),not -organism(X, noun_tops).
organism(X, noun_tops) :- organism(X),properties_tops(X),not -organism(X, noun_tops).
organism(X, noun_tops) :- plant(X, noun_tops).
physical_entity(X, noun_tops) :- object(X, noun_tops).
physical_entity(X, noun_tops) :- physical_entity(X),not -physical_entity(X, noun_tops).
physical_entity(X, noun_tops) :- physical_entity(X),properties_tops(X),not -physical_entity(X, noun_tops).
physical_entity(X, noun_tops) :- substance(X, noun_tops).
plant(X, noun_tops) :- plant(X),not -plant(X, noun_tops).
plant(X, noun_tops) :- plant(X),properties_tops(X),not -plant(X, noun_tops).
plant(X, noun_tops) :- vascular_plant(X, noun_plant).
produce(X, noun_food) :- edible_fruit(X, noun_food).
produce(X, noun_food) :- produce(X),not -produce(X, noun_food).
produce(X, noun_food) :- produce(X),properties_food(X),not -produce(X, noun_food).
social_group(X, noun_group) :- gathering(X, noun_group).
social_group(X, noun_group) :- social_group(X),not -social_group(X, noun_group).
social_group(X, noun_group) :- social_group(X),properties_group(X),not -social_group(X, noun_group).
solid(X, noun_substance) :- food(X, noun_food).
solid(X, noun_substance) :- solid(X),not -solid(X, noun_substance).
solid(X, noun_substance) :- solid(X),properties_substance(X),not -solid(X, noun_substance).
substance(X, noun_tops) :- food(X, noun_tops).
substance(X, noun_tops) :- solid(X, noun_substance).
substance(X, noun_tops) :- substance(X),not -substance(X, noun_tops).
substance(X, noun_tops) :- substance(X),properties_tops(X),not -substance(X, noun_tops).
table(X, noun_artifact) :- table(X),not -table(X, noun_artifact),not table(X, noun_object),not table(X, noun_group),not table(X, noun_food).
table(X, noun_artifact) :- table(X),properties_artifact(X),not -table(X, noun_artifact).
table(X, noun_food) :- table(X),not -table(X, noun_food),-table(X, noun_artifact),-table(X, noun_object),-table(X, noun_group).
table(X, noun_food) :- table(X),properties_food(X),not -table(X, noun_food).
table(X, noun_group) :- table(X),not -table(X, noun_group),-table(X, noun_artifact),-table(X, noun_object),not table(X, noun_food).
table(X, noun_group) :- table(X),properties_group(X),not -table(X, noun_group).
table(X, noun_object) :- table(X),not -table(X, noun_object),-table(X, noun_artifact),not table(X, noun_group),not table(X, noun_food).
table(X, noun_object) :- table(X),properties_object(X),not -table(X, noun_object).
tableland(X, noun_object) :- table(X, noun_object).
tableland(X, noun_object) :- tableland(X),not -tableland(X, noun_object).
tableland(X, noun_object) :- tableland(X),properties_object(X),not -tableland(X, noun_object).
tree(X, noun_plant) :- angiospermous_tree(X, noun_plant).
tree(X, noun_plant) :- tree(X),not -tree(X, noun_plant).
tree(X, noun_plant) :- tree(X),properties_plant(X),not -tree(X, noun_plant).
vascular_plant(X, noun_plant) :- herb(X, noun_plant).
vascular_plant(X, noun_plant) :- vascular_plant(X),not -vascular_plant(X, noun_plant).
vascular_plant(X, noun_plant) :- vascular_plant(X),properties_plant(X),not -vascular_plant(X, noun_plant).
vascular_plant(X, noun_plant) :- woody_plant(X, noun_plant).
whole(X, noun_tops) :- artifact(X, noun_tops).
whole(X, noun_tops) :- whole(X),not -whole(X, noun_tops).
whole(X, noun_tops) :- whole(X),properties_tops(X),not -whole(X, noun_tops).
woody_plant(X, noun_plant) :- tree(X, noun_plant).
woody_plant(X, noun_plant) :- woody_plant(X),not -woody_plant(X, noun_plant).
woody_plant(X, noun_plant) :- woody_plant(X),properties_plant(X),not -woody_plant(X, noun_plant).
