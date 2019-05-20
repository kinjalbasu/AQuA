%-------------------------------------------------------%
%Question Rules%
%-------------------------------------------------------%
_similar(X, X).
_similar(X, Y) :- _abbreviation(X, Y).
_similar(X, Y) :- _abbreviation(Y, X).
_similar(X, Y) :- _is(X, Y).
_similar(X, Z) :- _abbreviation(X, Y), _is(Y, Z).

query(N,Q , answer(A, certain)) :- question(Q, N), question(Q, certain, A).

query(N,Q, answer(A, likely)) :- question(Q, N), question(Q, likely, A),
						  not question(Q, certain, A).

query(N,Q, answer(A, possible)) :- question(Q, N), question(Q, possible, A),
						  not question(Q, likely, A),
						  not question(Q, certain, A).

query(N,Q, answer(A, guess)) :- question(Q, N), question(Q, guess, A),
						  not question(Q, possible, A),
						  not question(Q, likely, A),
						  not question(Q, certain, A).
						  
						  
						  
%Counting Rules%
member(X, [X|_]).
member(X, [_|T]) :- member(X,T).

nonmember(X,L) :- not member(X,L).

count(X,L1,Y,X) :- quantification(Y,X), nonmember([Y,X],L1).
count(X,L1,Y,Z) :- is_a(Z,X),quantification(Y,Z), nonmember([Y,Z],L1).

add_tail([],X,[X]).
add_tail([H|T],X,[H|L]) :- add_tail(T,X,L).

find_all(X,L1,L3) :- count(X,L1,Y,Z), add_tail(L1,[Y,Z],L2), find_all(X,L2,L3).
find_all(X,L1,L1).

find_all(X,L) :- find_all(X,[],L).

list_sum([], 0).
list_sum([[X,Y]], X).
list_sum([[X1,_],[X2,_] | T], Total) :- X3 is X1 + X2,
    list_sum([[X3|_]|T], Total).
	
total_element(X,Sum) :- find_all(X,L), list_sum(L,Sum).
