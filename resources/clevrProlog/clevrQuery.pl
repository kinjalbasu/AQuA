:- include('clevrKnowledge.pl').
:- include('clevrRules.pl').
:- include('clevrSemanticRules.pl').
:- include('clevrCommonFacts.pl').
find_ans(Q) :- question(Q),find_all_filters(cylinder, 7, L1),list_object(L1, Ids1),list_length(Ids1, C1),find_all_filters(cylinder, 15, L2),list_object(L2, Ids2),list_length(Ids2, C2),gt(C1, C2).
question('is the number of yellow metal cylinders greater than the number of yellow rubber cylinders ?').
question_answer('is the number of yellow metal cylinders greater than the number of yellow rubber cylinders ?', true) :- find_ans('is the number of yellow metal cylinders greater than the number of yellow rubber cylinders ?').
question_answer('is the number of yellow metal cylinders greater than the number of yellow rubber cylinders ?', false) :- not(find_ans('is the number of yellow metal cylinders greater than the number of yellow rubber cylinders ?')).
query(Q, A) :- question(Q),question_answer(Q, A).
