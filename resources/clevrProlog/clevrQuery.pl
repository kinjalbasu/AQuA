:- include('clevrKnowledge.pl').
:- include('clevrRules.pl').
:- include('clevrSemanticRules.pl').
:- include('clevrCommonFacts.pl').
find_ans(Q, A) :- question(Q),find_all_filters(cube, 3, L1),list_object(L1, L2),find_all_filters(thing, 6, L3),filter_all(L3, L2, Ids1),find_all_filters(object, 10, L4),filter_all(L4, L2, Ids2),union(Ids1, Ids2, Ids),list_length(Ids, A).
question('how many cubes are brown things or tiny metal objects ?').
question_answer('how many cubes are brown things or tiny metal objects ?', A) :- find_ans('how many cubes are brown things or tiny metal objects ?', A).
query(Q, A) :- question(Q),question_answer(Q, A).
