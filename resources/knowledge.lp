#include 'CommonRules.lp'.
#include 'question.lp'.
#include 'semanticRelations.lp'.
_mod(apple, 4).
_mod(banana, 3).
_mod(fork, 2).
_mod(spoon, 2).
_property(1, banana, with, '4_apple').
_property(1, be, on, table).
apple(apple).
banana(banana).
event(1, be, banana, null).
event(1, be, fork, null).
event(1, be, spoon, null).
fork(fork).
number(four).
number(three).
number(two).
spoon(spoon).
table(table).
abstract_entity(X, noun_tops) :- abstract_entity(X),not -abstract_entity(X, noun_tops).
abstract_entity(X, noun_tops) :- abstract_entity(X),properties_tops(X),not -abstract_entity(X, noun_tops).
abstract_entity(X, noun_tops) :- abstraction(X, noun_tops).
abstraction(X, noun_tops) :- abstraction(X),not -abstraction(X, noun_tops).
abstraction(X, noun_tops) :- abstraction(X),properties_tops(X),not -abstraction(X, noun_tops).
abstraction(X, noun_tops) :- attribute(X, noun_tops).
abstraction(X, noun_tops) :- group(X, noun_tops).
abstraction(X, noun_tops) :- measure(X, noun_tops).
abstraction(X, noun_tops) :- psychological_feature(X, noun_tops).
act(X, noun_tops) :- act(X),not -act(X, noun_tops).
act(X, noun_tops) :- act(X),properties_tops(X),not -act(X, noun_tops).
act(X, noun_tops) :- action(X, noun_act).
action(X, noun_act) :- action(X),not -action(X, noun_act).
action(X, noun_act) :- action(X),properties_act(X),not -action(X, noun_act).
action(X, noun_act) :- change(X, noun_act).
amorphous_shape(X, noun_shape) :- amorphous_shape(X),not -amorphous_shape(X, noun_shape).
amorphous_shape(X, noun_shape) :- amorphous_shape(X),properties_shape(X),not -amorphous_shape(X, noun_shape).
amorphous_shape(X, noun_shape) :- space(X, noun_shape).
angiospermous_tree(X, noun_plant) :- angiospermous_tree(X),not -angiospermous_tree(X, noun_plant).
angiospermous_tree(X, noun_plant) :- angiospermous_tree(X),properties_plant(X),not -angiospermous_tree(X, noun_plant).
angiospermous_tree(X, noun_plant) :- fruit_tree(X, noun_plant).
angle(X, noun_shape) :- angle(X),not -angle(X, noun_shape).
angle(X, noun_shape) :- angle(X),properties_shape(X),not -angle(X, noun_shape).
angle(X, noun_shape) :- fork(X, noun_body).
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
article(X, noun_tops) :- article(X),not -article(X, noun_tops).
article(X, noun_tops) :- article(X),properties_tops(X),not -article(X, noun_tops).
article(X, noun_tops) :- ware(X, noun_artifact).
artifact(X, noun_tops) :- article(X, noun_tops).
artifact(X, noun_tops) :- artifact(X),not -artifact(X, noun_tops).
artifact(X, noun_tops) :- artifact(X),properties_tops(X),not -artifact(X, noun_tops).
artifact(X, noun_tops) :- instrumentality(X, noun_artifact).
attribute(X, noun_tops) :- attribute(X),not -attribute(X, noun_tops).
attribute(X, noun_tops) :- attribute(X),properties_tops(X),not -attribute(X, noun_tops).
attribute(X, noun_tops) :- shape(X, noun_tops).
banana(X, noun_food) :- banana(X),not -banana(X, noun_food),-banana(X, noun_plant).
banana(X, noun_food) :- banana(X),properties_food(X),not -banana(X, noun_food).
banana(X, noun_plant) :- banana(X),not -banana(X, noun_plant),not banana(X, noun_food).
banana(X, noun_plant) :- banana(X),properties_plant(X),not -banana(X, noun_plant).
branch(X, noun_shape) :- branch(X),not -branch(X, noun_shape).
branch(X, noun_shape) :- branch(X),properties_shape(X),not -branch(X, noun_shape).
branch(X, noun_shape) :- fork(X, noun_shape).
change(X, noun_act) :- change(X),not -change(X, noun_act).
change(X, noun_act) :- change(X),properties_act(X),not -change(X, noun_act).
change(X, noun_act) :- change_of_integrity(X, noun_act).
change_of_integrity(X, noun_act) :- change_of_integrity(X),not -change_of_integrity(X, noun_act).
change_of_integrity(X, noun_act) :- change_of_integrity(X),properties_act(X),not -change_of_integrity(X, noun_act).
change_of_integrity(X, noun_act) :- separation(X, noun_act).
containerful(X, noun_quantity) :- containerful(X),not -containerful(X, noun_quantity).
containerful(X, noun_quantity) :- containerful(X),properties_quantity(X),not -containerful(X, noun_quantity).
containerful(X, noun_quantity) :- spoon(X, noun_quantity).
cutlery(X, noun_artifact) :- cutlery(X),not -cutlery(X, noun_artifact).
cutlery(X, noun_artifact) :- cutlery(X),properties_artifact(X),not -cutlery(X, noun_artifact).
cutlery(X, noun_artifact) :- fork(X, noun_artifact).
cutlery(X, noun_artifact) :- spoon(X, noun_artifact).
division(X, noun_act) :- division(X),not -division(X, noun_act).
division(X, noun_act) :- division(X),properties_act(X),not -division(X, noun_act).
division(X, noun_act) :- fork(X, noun_act).
edible_fruit(X, noun_food) :- apple(X, noun_food).
edible_fruit(X, noun_food) :- banana(X, noun_food).
edible_fruit(X, noun_food) :- edible_fruit(X),not -edible_fruit(X, noun_food).
edible_fruit(X, noun_food) :- edible_fruit(X),properties_food(X),not -edible_fruit(X, noun_food).
entity(X, noun_tops) :- abstract_entity(X, noun_tops).
entity(X, noun_tops) :- entity(X),not -entity(X, noun_tops).
entity(X, noun_tops) :- entity(X),properties_tops(X),not -entity(X, noun_tops).
entity(X, noun_tops) :- physical_entity(X, noun_tops).
equipment(X, noun_artifact) :- equipment(X),not -equipment(X, noun_artifact).
equipment(X, noun_artifact) :- equipment(X),properties_artifact(X),not -equipment(X, noun_artifact).
equipment(X, noun_artifact) :- sports_equipment(X, noun_artifact).
event(X, noun_tops) :- act(X, noun_tops).
event(X, noun_tops) :- event(X),not -event(X, noun_tops).
event(X, noun_tops) :- event(X),properties_tops(X),not -event(X, noun_tops).
fare(X, noun_food) :- fare(X),not -fare(X, noun_food).
fare(X, noun_food) :- fare(X),properties_food(X),not -fare(X, noun_food).
fare(X, noun_food) :- table(X, noun_food).
figure(X, noun_shape) :- figure(X),not -figure(X, noun_shape).
figure(X, noun_shape) :- figure(X),properties_shape(X),not -figure(X, noun_shape).
figure(X, noun_shape) :- subfigure(X, noun_shape).
food(X, noun_food) :- food(X),not -food(X, noun_food),-food(X, noun_tops).
food(X, noun_food) :- food(X),properties_food(X),not -food(X, noun_food).
food(X, noun_food) :- produce(X, noun_food).
food(X, noun_tops) :- fare(X, noun_food).
food(X, noun_tops) :- food(X),not -food(X, noun_tops),not food(X, noun_food).
food(X, noun_tops) :- food(X),properties_tops(X),not -food(X, noun_tops).
fork(X, noun_act) :- fork(X),not -fork(X, noun_act),not fork(X, noun_shape),not fork(X, noun_artifact),not fork(X, noun_body).
fork(X, noun_act) :- fork(X),properties_act(X),not -fork(X, noun_act).
fork(X, noun_artifact) :- fork(X),not -fork(X, noun_artifact),-fork(X, noun_act),-fork(X, noun_shape),not fork(X, noun_body).
fork(X, noun_artifact) :- fork(X),properties_artifact(X),not -fork(X, noun_artifact).
fork(X, noun_body) :- fork(X),not -fork(X, noun_body),-fork(X, noun_act),-fork(X, noun_shape),-fork(X, noun_artifact).
fork(X, noun_body) :- fork(X),properties_body(X),not -fork(X, noun_body).
fork(X, noun_shape) :- fork(X),not -fork(X, noun_shape),-fork(X, noun_act),not fork(X, noun_artifact),not fork(X, noun_body).
fork(X, noun_shape) :- fork(X),properties_shape(X),not -fork(X, noun_shape).
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
golf_club(X, noun_artifact) :- golf_club(X),not -golf_club(X, noun_artifact).
golf_club(X, noun_artifact) :- golf_club(X),properties_artifact(X),not -golf_club(X, noun_artifact).
golf_club(X, noun_artifact) :- wood(X, noun_artifact).
golf_equipment(X, noun_artifact) :- golf_club(X, noun_artifact).
golf_equipment(X, noun_artifact) :- golf_equipment(X),not -golf_equipment(X, noun_artifact).
golf_equipment(X, noun_artifact) :- golf_equipment(X),properties_artifact(X),not -golf_equipment(X, noun_artifact).
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
implement(X, noun_artifact) :- implement(X),not -implement(X, noun_artifact).
implement(X, noun_artifact) :- implement(X),properties_artifact(X),not -implement(X, noun_artifact).
implement(X, noun_artifact) :- tool(X, noun_artifact).
indefinite_quantity(X, noun_quantity) :- containerful(X, noun_quantity).
indefinite_quantity(X, noun_quantity) :- indefinite_quantity(X),not -indefinite_quantity(X, noun_quantity).
indefinite_quantity(X, noun_quantity) :- indefinite_quantity(X),properties_quantity(X),not -indefinite_quantity(X, noun_quantity).
instrumentality(X, noun_artifact) :- equipment(X, noun_artifact).
instrumentality(X, noun_artifact) :- furnishing(X, noun_artifact).
instrumentality(X, noun_artifact) :- implement(X, noun_artifact).
instrumentality(X, noun_artifact) :- instrumentality(X),not -instrumentality(X, noun_artifact).
instrumentality(X, noun_artifact) :- instrumentality(X),properties_artifact(X),not -instrumentality(X, noun_artifact).
living_thing(X, noun_tops) :- living_thing(X),not -living_thing(X, noun_tops).
living_thing(X, noun_tops) :- living_thing(X),properties_tops(X),not -living_thing(X, noun_tops).
living_thing(X, noun_tops) :- organism(X, noun_tops).
measure(X, noun_tops) :- indefinite_quantity(X, noun_quantity).
measure(X, noun_tops) :- measure(X),not -measure(X, noun_tops).
measure(X, noun_tops) :- measure(X),properties_tops(X),not -measure(X, noun_tops).
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
psychological_feature(X, noun_tops) :- event(X, noun_tops).
psychological_feature(X, noun_tops) :- psychological_feature(X),not -psychological_feature(X, noun_tops).
psychological_feature(X, noun_tops) :- psychological_feature(X),properties_tops(X),not -psychological_feature(X, noun_tops).
separation(X, noun_act) :- division(X, noun_act).
separation(X, noun_act) :- separation(X),not -separation(X, noun_act).
separation(X, noun_act) :- separation(X),properties_act(X),not -separation(X, noun_act).
shape(X, noun_tops) :- amorphous_shape(X, noun_shape).
shape(X, noun_tops) :- figure(X, noun_shape).
shape(X, noun_tops) :- shape(X),not -shape(X, noun_tops).
shape(X, noun_tops) :- shape(X),properties_tops(X),not -shape(X, noun_tops).
social_group(X, noun_group) :- gathering(X, noun_group).
social_group(X, noun_group) :- social_group(X),not -social_group(X, noun_group).
social_group(X, noun_group) :- social_group(X),properties_group(X),not -social_group(X, noun_group).
solid(X, noun_substance) :- food(X, noun_food).
solid(X, noun_substance) :- solid(X),not -solid(X, noun_substance).
solid(X, noun_substance) :- solid(X),properties_substance(X),not -solid(X, noun_substance).
space(X, noun_shape) :- angle(X, noun_shape).
space(X, noun_shape) :- space(X),not -space(X, noun_shape).
space(X, noun_shape) :- space(X),properties_shape(X),not -space(X, noun_shape).
spoon(X, noun_artifact) :- spoon(X),not -spoon(X, noun_artifact),-spoon(X, noun_quantity).
spoon(X, noun_artifact) :- spoon(X),properties_artifact(X),not -spoon(X, noun_artifact).
spoon(X, noun_quantity) :- spoon(X),not -spoon(X, noun_quantity),not spoon(X, noun_artifact).
spoon(X, noun_quantity) :- spoon(X),properties_quantity(X),not -spoon(X, noun_quantity).
sports_equipment(X, noun_artifact) :- golf_equipment(X, noun_artifact).
sports_equipment(X, noun_artifact) :- sports_equipment(X),not -sports_equipment(X, noun_artifact).
sports_equipment(X, noun_artifact) :- sports_equipment(X),properties_artifact(X),not -sports_equipment(X, noun_artifact).
subfigure(X, noun_shape) :- branch(X, noun_shape).
subfigure(X, noun_shape) :- subfigure(X),not -subfigure(X, noun_shape).
subfigure(X, noun_shape) :- subfigure(X),properties_shape(X),not -subfigure(X, noun_shape).
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
tableware(X, noun_artifact) :- cutlery(X, noun_artifact).
tableware(X, noun_artifact) :- tableware(X),not -tableware(X, noun_artifact).
tableware(X, noun_artifact) :- tableware(X),properties_artifact(X),not -tableware(X, noun_artifact).
tool(X, noun_artifact) :- fork(X, noun_artifact).
tool(X, noun_artifact) :- tool(X),not -tool(X, noun_artifact).
tool(X, noun_artifact) :- tool(X),properties_artifact(X),not -tool(X, noun_artifact).
tree(X, noun_plant) :- angiospermous_tree(X, noun_plant).
tree(X, noun_plant) :- tree(X),not -tree(X, noun_plant).
tree(X, noun_plant) :- tree(X),properties_plant(X),not -tree(X, noun_plant).
vascular_plant(X, noun_plant) :- herb(X, noun_plant).
vascular_plant(X, noun_plant) :- vascular_plant(X),not -vascular_plant(X, noun_plant).
vascular_plant(X, noun_plant) :- vascular_plant(X),properties_plant(X),not -vascular_plant(X, noun_plant).
vascular_plant(X, noun_plant) :- woody_plant(X, noun_plant).
ware(X, noun_artifact) :- tableware(X, noun_artifact).
ware(X, noun_artifact) :- ware(X),not -ware(X, noun_artifact).
ware(X, noun_artifact) :- ware(X),properties_artifact(X),not -ware(X, noun_artifact).
whole(X, noun_tops) :- artifact(X, noun_tops).
whole(X, noun_tops) :- whole(X),not -whole(X, noun_tops).
whole(X, noun_tops) :- whole(X),properties_tops(X),not -whole(X, noun_tops).
wood(X, noun_artifact) :- spoon(X, noun_artifact).
wood(X, noun_artifact) :- wood(X),not -wood(X, noun_artifact).
wood(X, noun_artifact) :- wood(X),properties_artifact(X),not -wood(X, noun_artifact).
woody_plant(X, noun_plant) :- tree(X, noun_plant).
woody_plant(X, noun_plant) :- woody_plant(X),not -woody_plant(X, noun_plant).
woody_plant(X, noun_plant) :- woody_plant(X),properties_plant(X),not -woody_plant(X, noun_plant).
