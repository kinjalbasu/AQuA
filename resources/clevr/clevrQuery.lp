:- include('clevrKnowledge.pl').
:- include('clevrRules.pl').
:- include('clevrSemanticRules.pl').
:- include('clevrCommonFacts.pl').
find_ans(Q) :- question(Q),find_all_filters(thing, 7, L1),list_object(L1, Ids1),list_length(Ids1, C1),find_all_filters(object, 10, L2),list_object(L2, Ids2),list_length(Ids2, C2),gt(C1, C2).
question('are there more large yellow matte things than purple objects ?').
question_answer('are there more large yellow matte things than purple objects ?', true) :- find_ans('are there more large yellow matte things than purple objects ?').
question_answer('are there more large yellow matte things than purple objects ?', false) :- not find_ans('are there more large yellow matte things than purple objects ?').
query(Q, A) :- question(Q),question_answer(Q, A).
