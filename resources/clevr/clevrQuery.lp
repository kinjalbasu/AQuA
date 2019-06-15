#include 'clevrKnowledge.lp'.
#include 'clevrRules.lp'.
#include 'clevrSemanticRules.lp'.
#include 'clevrCommonFacts.lp'.


%?- get_all_id(L).
%?- list_object([[color,red],[material,metal]],L).
%?- order_by_size(Small_to_large).
%?- order_left_to_right(Left_to_Right).
%?- order_bottom_to_top(Bottom_to_Top).
%?- count_object(cube,_,_,L).
%?- count(cube,red,_,[],X).
%?- greater_than_count([[color,red],[material,metal]],Th), Th > 5.
%?- list_length([1],N).
%?- test_gt(1).

%?- find_all_filters(cube,L), list_object(L,Ids), list_length(Ids,C) , quantification(N,cube), gte(C,N).   
?- find_all_filters(cube,L), list_object(L,Ids), list_length(Ids,C) , C >= 5.   
%?- find_all_filters(block,L). 
%?- is_property(cube,shape).
%?- find_all_filters(cube,[[shape,cube]],L).
%?- find_all_filters(cube,L).