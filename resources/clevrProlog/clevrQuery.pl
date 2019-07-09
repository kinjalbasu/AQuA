:- include('clevrKnowledge.pl').
:- include('clevrRules.pl').
:- include('clevrSemanticRules.pl').
:- include('clevrCommonFacts.pl').
find_ans(Q) :- question(Q),find_all_filters(block, 5, L1),list_object(L1, Ids1),list_length(Ids1, C1),find_all_filters(cylinder, 12, L2),list_object(L2, Ids2),list_length(Ids2, C2),gt(C1, C2).
question('is the number of blocks greater than the number of yellow cylinders ?').
question_answer('is the number of blocks greater than the number of yellow cylinders ?', true) :- find_ans('is the number of blocks greater than the number of yellow cylinders ?').
question_answer('is the number of blocks greater than the number of yellow cylinders ?', false) :- not(find_ans('is the number of blocks greater than the number of yellow cylinders ?')).
query(Q, A) :- question(Q),question_answer(Q, A).
