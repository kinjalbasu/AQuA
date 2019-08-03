:- include('clevrKnowledge.pl').
:- include('clevrRules.pl').
:- include('clevrSemanticRules.pl').
:- include('clevrCommonFacts.pl').
find_ans(Q, A) :- question(Q),find_all_filters(cylinder, 4, L),list_object(L, Ids),list_length(Ids, A).
question('what number of cylinders are there ?').
question_answer('what number of cylinders are there ?', A) :- find_ans('what number of cylinders are there ?', A).
query(Q, A) :- question(Q),question_answer(Q, A).
