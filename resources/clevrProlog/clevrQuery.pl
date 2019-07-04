:- include('clevrKnowledge.pl').
:- include('clevrRules.pl').
:- include('clevrSemanticRules.pl').
:- include('clevrCommonFacts.pl').
find_ans(Q) :- question(Q),find_all_filters(ball, 7, L1),find_all_filters(object, 13, L2),list_object(L1, Ids1),list_object(L2, Ids2),get_att_val(Ids1, size, Val),get_att_val(Ids2, size, Val).
question('is the size of the blue ball the same as the pink object ?').
question_answer('is the size of the blue ball the same as the pink object ?', true) :- find_ans('is the size of the blue ball the same as the pink object ?').
question_answer('is the size of the blue ball the same as the pink object ?', false) :- not(find_ans('is the size of the blue ball the same as the pink object ?')).
query(Q, A) :- question(Q),question_answer(Q, A).
