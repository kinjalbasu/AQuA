_pos(there,ex).
_pos(be,vbp).
_pos(2,cd).
_pos(fork,nns).
_pos(2,cd).
_pos(spoon,nns).
_pos(and,cc).
_pos(3,cd).
_pos(banana,nns).
_pos(with,in).
_pos(4,cd).
_pos(apple,nns).
_pos(on,in).
_pos(the,dt).
_pos(table,nn).


_expl(be,there).
_nummod(fork,2).
_nsubj(be,fork).
_nummod(spoon,2).
_nsubj(be,spoon).
_conj_and(fork,spoon).
_cc(fork,and).
_nummod(banana,3).
_nsubj(be,banana).
_conj_and(fork,banana).
_case(apple,with).
_nummod(apple,4).
_nmod_with(banana,apple).
_case(table,on).
_det(table,the).
_nmod_on(be,table).




is_a(fork,metallic_object).
is_a(fork,utensil).
is_a(fork,item).
is_a(fork,tool).
is_a(fork,object).
is_a(spoon,utensil).
is_a(spoon,metallic_object).
is_a(spoon,item).
is_a(spoon,food_utensil).
is_a(spoon,object).
is_a(banana,fresh_fruit).
is_a(banana,fruit).
is_a(banana,tropical_fruit).
is_a(banana,food).
is_a(banana,crop).
is_a(apple,fresh_fruit).
is_a(apple,fruit).
is_a(apple,company).
is_a(apple,brand).
is_a(apple,food).
is_a(table,affordable_and_multifunctional_furniture_piece).
is_a(table,item).
is_a(table,furniture).
is_a(table,database_object).
is_a(table,object).


event(1, be, banana, null).
event(1, be, fork, null).
event(1, be, spoon, null).

is_a(fork,metallic_object).
is_a(fork,utensil).
is_a(fork,item).
is_a(fork,tool).
is_a(fork,object).
is_a(spoon,utensil).
is_a(spoon,metallic_object).
is_a(spoon,item).
is_a(spoon,food_utensil).
is_a(spoon,object).
is_a(banana,fresh_fruit).
is_a(banana,fruit).
is_a(banana,tropical_fruit).
is_a(banana,food).
is_a(banana,crop).
is_a(apple,fresh_fruit).
is_a(apple,fruit).
is_a(apple,company).
is_a(apple,brand).
is_a(apple,food).
is_a(table,affordable_and_multifunctional_furniture_piece).
is_a(table,item).
is_a(table,furniture).
is_a(table,database_object).
is_a(table,object).
event(1,be,banana,null).
event(1,be,fork,null).
event(1,be,spoon,null).
agent(fork,1).
agent(spoon,1).
agent(banana,1).
quantification(2,fork).
quantification(2,spoon).
quantification(3,banana).
quantification(4,apple).
