_pos(there,ex).
_pos(be,vbp).
_pos(four,cd).
_pos(apple,nns).
_pos(five,cd).
_pos(banana,nns).
_pos(on,in).
_pos(the,dt).
_pos(table,nn).


_expl(be,there).
_nummod(apple,four).
_nsubj(be,apple).
_nummod(banana,five).
_appos(apple,banana).
_case(table,on).
_det(table,the).
_nmod_on(banana,table).




is_a(apple,fruit).
is_a(banana,fruit).
is_a(table,affordable_and_multifunctional_furniture_piece).


event(1, be, apple, null).
event(1, be, banana, null).

is_a(apple,fruit).
is_a(banana,fruit).
is_a(table,affordable_and_multifunctional_furniture_piece).
event(1,be,apple,null).
event(1,be,banana,null).
agent(apple,1).
quantification(four,apple).
quantification(five,banana).
