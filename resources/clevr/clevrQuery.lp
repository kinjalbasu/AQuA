#include 'clevrKnowledge.lp'.
#include 'clevrRules.lp'.

%?- get_all_id(L).
?- list_object([[color,red],[material,metal]],L).
%?- order_by_size(Small_to_large).
%?- order_left_to_right(Left_to_Right).
%?- order_bottom_to_top(Bottom_to_Top).
%?- count_object(cube,_,_,L).
%?- count(cube,red,_,[],X).
