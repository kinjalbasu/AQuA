:- include('clevrKnowledge.pl').
:- include('clevrRules.pl').
:- include('clevrSemanticRules.pl').
:- include('clevrCommonFacts.pl').
find_ans(Q, A) :- question(Q),find_all_filters(thing, 4, L1),list_object(L1, L2),find_all_filters(object, 8, L3),filter_all(L3, L2, Ids1),find_all_filters(cube, 11, L4),filter_all(L4, L2, Ids2),union(Ids1, Ids2, Ids),list_length(Ids, A).
question('how many yellow things are tiny metal objects or big cubes ?').
question_answer('how many yellow things are tiny metal objects or big cubes ?', A) :- find_ans('how many yellow things are tiny metal objects or big cubes ?', A).
query(Q, A) :- question(Q),question_answer(Q, A).
