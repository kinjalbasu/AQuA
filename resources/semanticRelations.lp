_pos(there,ex).
_pos(be,vbp).
_pos(4,cd).
_pos(apple,nns).
_pos(5,cd).
_pos(banana,nns).
_pos(on,in).
_pos(the,dt).
_pos(table,nn).


_expl(be,there).
_nummod(apple,4).
_nsubj(be,apple).
_nummod(banana,5).
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
quantification(4,apple).
quantification(5,banana).
