:- include('clevrKnowledge.pl').
:- include('clevrRules.pl').
:- include('clevrSemanticRules.pl').
:- include('clevrCommonFacts.pl').
find_ans(Q) :- question(Q),find_all_filters(cylinder, 4, L1),find_all_filters(sphere, 11, L2),list_object(L1, Ids1),list_object(L2, Ids2),get_att_val(Ids1, size, Val),get_att_val(Ids2, size, Val).
question('is the yellow cylinder the same size as the nonmetal sphere ?').
question_answer('is the yellow cylinder the same size as the nonmetal sphere ?', true) :- find_ans('is the yellow cylinder the same size as the nonmetal sphere ?').
question_answer('is the yellow cylinder the same size as the nonmetal sphere ?', false) :- not(find_ans('is the yellow cylinder the same size as the nonmetal sphere ?')).
query(Q, A) :- question(Q),question_answer(Q, A).
