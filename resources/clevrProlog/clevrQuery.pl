:- include('clevrKnowledge.pl').
:- include('clevrRules.pl').
:- include('clevrSemanticRules.pl').
:- include('clevrCommonFacts.pl').
find_ans(Q, A) :- question(Q),find_all_filters(ball, 6, L),list_object(L, Ids),list_length(Ids, A).
question('how many small blue metallic balls are there ?').
question_answer('how many small blue metallic balls are there ?', A) :- find_ans('how many small blue metallic balls are there ?', A).
query(Q, A) :- question(Q),question_answer(Q, A).
