:- include('clevrKnowledge.pl').
:- include('clevrRules.pl').
:- include('clevrSemanticRules.pl').
:- include('clevrCommonFacts.pl').
find_ans(Q) :- question(Q),find_all_filters(block, 4, L1),find_all_filters(thing, 11, L2),list_object(L1, Ids1),list_object(L2, Ids2),get_att_val(Ids1, size, Val),get_att_val(Ids2, size, Val).
question('is the blue block the same size as the pink thing ?').
question_answer('is the blue block the same size as the pink thing ?', true) :- find_ans('is the blue block the same size as the pink thing ?').
question_answer('is the blue block the same size as the pink thing ?', false) :- not(find_ans('is the blue block the same size as the pink thing ?')).
query(Q, A) :- question(Q),question_answer(Q, A).
