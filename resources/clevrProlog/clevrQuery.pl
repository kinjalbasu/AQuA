:- include('clevrKnowledge.pl').
:- include('clevrRules.pl').
:- include('clevrSemanticRules.pl').
:- include('clevrCommonFacts.pl').
find_ans(Q) :- question(Q),find_all_filters(object, 5, L1),find_all_filters(cylinder, 14, L2),list_object(L1, Ids1),list_object(L2, Ids2),get_att_val(Ids1, material, Val),get_att_val(Ids2, material, Val).
question('is the large red object made of the same material as the blue cylinder ?').
question_answer('is the large red object made of the same material as the blue cylinder ?', true) :- find_ans('is the large red object made of the same material as the blue cylinder ?').
question_answer('is the large red object made of the same material as the blue cylinder ?', false) :- not(find_ans('is the large red object made of the same material as the blue cylinder ?')).
query(Q, A) :- question(Q),question_answer(Q, A).
