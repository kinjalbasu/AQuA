:- include('clevrKnowledge.pl').
:- include('clevrRules.pl').
:- include('clevrSemanticRules.pl').
:- include('clevrCommonFacts.pl').
find_ans(Q) :- question(Q),find_all_filters(thing, 6, L1),list_object(L1, Ids1),list_length(Ids1, C1),find_all_filters(object, 13, L2),list_object(L2, Ids2),list_length(Ids2, C2),lt(C1, C2).
question('is the number of red things less than the number of blue objects ?').
question_answer('is the number of red things less than the number of blue objects ?', true) :- find_ans('is the number of red things less than the number of blue objects ?').
question_answer('is the number of red things less than the number of blue objects ?', false) :- not(find_ans('is the number of red things less than the number of blue objects ?')).
query(Q, A) :- question(Q),question_answer(Q, A).
