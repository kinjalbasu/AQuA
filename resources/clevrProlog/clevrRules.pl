property(Id,shape,Shape) :- object(Id,Shape,_,_,_,_,_).
property(Id,color,Color) :- object(Id,_,Color,_,_,_,_).
property(Id,material,Material) :- object(Id,_,_,Material,_,_,_).
property(Id,size,Size) :- object(Id,_,_,_,Size,_,_).
property(Id,centerX,X) :- object(Id,_,_,_,_,X,_).
property(Id,centerY,Y) :- object(Id,_,_,_,_,_,Y).
				 							
size_order([small, medium, big]).
											
													
%~~~~~UTILITY PREDICATES~~~~~~~~~																		
member(X, [X|_]).
member(X, [_|T]) :- member(X,T).

nonmember(X,L) :- not(member(X,L)).

union([],[],[]).
union(List1,[],List1).
union(List1, [Head2|Tail2], [Head2|Output]):-
    nonmember(Head2,List1), union(List1,Tail2,Output).
union(List1, [Head2|Tail2], Output):-
    member(Head2,List1), union(List1,Tail2,Output).  


indexOf([Element|_], Element, 0). 
indexOf([_|Tail], Element, Index):-
  indexOf(Tail, Element, Index1), 
  Index is Index1+1. 
  
append([],X,X).
append([X|L1],L2,[X|L3]):-
        append(L1,L2,L3).

add_tail([],X,[X]).
add_tail([H|T],X,[H|L]) :- add_tail(T,X,L).

gt(A,B) :- A > B.
gte(A,B) :- A >= B.
lt(A,B) :- not(gte(A,B)).
lte(A,B) :- not(gt(A,B)).

%~~~~~GET Ids~~~~~~~~~
%count_id(L1,Id) :- property(Id,_,_), nonmember(Id,L1).
%get_all_id(L1,L3) :- count_id(L1,Id), add_tail(L1,Id,L2), get_all_id(L2,L3).
%get_all_id(L1,L1).
%get_all_id(L) :- get_all_id([],L).
%get_all_id([1,2,3,4,5]).

%~~~SORTING~~~~~~~~
list_length([],0).
list_length([_|Xs],L) :- list_length(Xs,N) , L is N+1 .


mergesort([],[],Param).
mergesort([X],[X],Param).
mergesort([X|Xs],S,Param) :-
    list_length(Xs,Len),
    0 < Len,
    split_in_half([X|Xs],Ys,Zs),
    mergesort(Ys,SY,Param),
    mergesort(Zs,SZ,Param),
    merge(SY, SZ,S, Param).
merge([], Xs, Xs, Param).
merge(Xs,[],Xs, Param).

split_in_half(Xs, Ys, Zs) :-
    list_length(Xs, Len),
    Half is Len // 2, 
    split_at(Xs, Half, Ys, Zs).

split_at(Xs, N, Ys, Zs) :-
    list_length(Ys, N),
    append(Ys, Zs, Xs).
	
merge([X|Xs],[Y|Ys],[X|S],Param) :-
    compare_val(X,Y,Param),
    merge(Xs,[Y|Ys],S,Param).
merge([X|Xs],[Y|Ys],[Y|S],Param) :-
    compare_val(Y,X,Param),
    merge([X|Xs], Ys, S, Param).

compare_val(Id1,Id2,size) :- smaller_equal(Id1,Id2).	
compare_val(Id1,Id2,x_axis) :- left_compare(Id1,Id2).	
compare_val(Id1,Id2,y_axis) :- behind_compare(Id1,Id2).	


%~~~~~~~~~~~COUNTING~~~~~~~~~~~~~~

filter(_,_,[],[]).
filter(Att,Val,[Id|T1],[Id|T2]) :- property(Id,Att,Val), filter(Att,Val,T1,T2).
filter(Att,Val,[Id|T1],T2) :- not(property(Id,Att,Val)),filter(Att,Val,T1,T2).

filter_all([],Ids,Ids).
filter_all([[Att,Val]|T],Ids,L) :- filter(Att,Val,Ids,L1),filter_all(T,L1,L). 

%check([],_).
%check([[Att,Val]|T],Id) :- property(Id,Att,Val), check(T,Id).

%filter_all(_,[],[]).
%filter_all(Filters,[Id|T1],[Id|T2]) :- check(Filters,Id),filter_all(Filters,T1,T2).
%filter_all(Filters,[Id|T1],T2) :- not(check(Filters,Id)),filter_all(Filters,T1,T2).



list_object(Filters,L) :- get_all_id(Ids), filter_all(Filters,Ids,L).


%----Compare----
larger(Id1,Id2) :- property(Id1,size,S1), property(Id2,size,S2), size_order(L), indexOf(L,S1,X1), indexOf(L,S2,X2), X1 > X2.
smaller(Id1,Id2) :- property(Id1,size,S1),property(Id2,size,S2), size_order(L), indexOf(L,S1,X1), indexOf(L,S2,X2), X1 < X2.
larger_equal(Id1,Id2) :- property(Id1,size,S1),property(Id2,size,S2), size_order(L), indexOf(L,S1,X1), indexOf(L,S2,X2), X1 >= X2.
smaller_equal(Id1,Id2) :- property(Id1,size,S1), property(Id2,size,S2), size_order(L), indexOf(L,S1,X1), indexOf(L,S2,X2), X1 =< X2.

left_compare(Id1,Id2) :- property(Id1,centerX,X1),property(Id2,centerX,X2),X1 > X2.
right_compare(Id1,Id2) :- property(Id1,centerX,X1),property(Id2,centerX,X2),X1 < X2.

behind_compare(Id1,Id2) :- property(Id1,centerY,Y1),property(Id2,centerY,Y2),Y1 > Y2.
front_compare(Id1,Id2) :- property(Id1,centerY,Y1),property(Id2,centerY,Y2),Y1 < Y2.

%----Find Order----------
order_by_size(L) :- get_all_id(L1), mergesort(L1,L,size).
order_left_to_right(L) :- get_all_id(L1), mergesort(L1,L,x_axis).
order_bottom_to_top(L) :- get_all_id(L1), mergesort(L1,L,y_axis).


%----Comparison on Count----

%greater_than_count(Filters,Th) :- list_object(Filters,L), list_length(L,Th).

%-------Filter List Creation---------


get_properties([],[]).
get_properties([X|T1],[[Y,X]|T2]) :- is_property(X,Y),get_properties(T1,T2).
get_properties([X|T1],[[Y,Z]|T2]) :- is_property(Z,Y), similar(Z,X),get_properties(T1,T2).
get_properties([X|T1],T2) :- not(is_property(X,Y)),not(similar(_,X)),get_properties(T1,T2).

%---shape checking------
check_shape(X,X) :- is_property(X,shape).
check_shape(X,Y) :- is_property(Y,shape),similar(X,Y).
%--------

filters(X,Id,L) :- values(X,Id,L1), get_properties(L1,L).
find_all_filters(X,Id,[[shape,Y]|L]) :-  check_shape(X,Y), filters(X,Id,L).
find_all_filters(X,Id,L) :- not(check_shape(X,Y)), filters(X,Id,L).

%Get value of an attribute of the first Id of the list.
get_att_val([H|T],Att,Val) :- property(H,Att,Val).

%--------List Substract-----------
list_subtract([],_,[]).
list_subtract([H|T1],L,[H|T2]) :- nonmember(H,L), list_subtract(T1,L,T2).
list_subtract([H|T1],L,T2) :- member(H,L), list_subtract(T1,L,T2).


%-------Get List After/Before,left/right Position-----------
get_behind_list(Id,L) :- get_all_id(L1),sub_list_behind(Id,L1,L).
get_front_list(Id,L) :- get_all_id(L1),sub_list_front(Id,L1,L).
get_left_list(Id,L) :- get_all_id(L1),sub_list_left(Id,L1,L).
get_right_list(Id,L) :- get_all_id(L1),sub_list_right(Id,L1,L).

sub_list_behind(_,[],[]).
sub_list_behind(Id,[H|T1],[H|T]) :- behind_compare(Id,H), sub_list_behind(Id,T1,T).
sub_list_behind(Id,[H|T1],T) :- not(behind_compare(Id,H)), sub_list_behind(Id,T1,T).

sub_list_front(_,[],[]).
sub_list_front(Id,[H|T1],[H|T]) :- front_compare(Id,H), sub_list_front(Id,T1,T).
sub_list_front(Id,[H|T1],T) :- not(front_compare(Id,H)), sub_list_front(Id,T1,T).

sub_list_right(_,[],[]).
sub_list_right(Id,[H|T1],[H|T]) :- right_compare(Id,H), sub_list_right(Id,T1,T).
sub_list_right(Id,[H|T1],T) :- not(right_compare(Id,H)), sub_list_right(Id,T1,T).

sub_list_left(_,[],[]).
sub_list_left(Id,[H|T1],[H|T]) :- left_compare(Id,H), sub_list_left(Id,T1,T).
sub_list_left(Id,[H|T1],T) :- not(left_compare(Id,H)), sub_list_left(Id,T1,T).


