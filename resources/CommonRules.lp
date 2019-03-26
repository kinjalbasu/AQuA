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