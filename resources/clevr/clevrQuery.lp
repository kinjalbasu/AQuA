:- include('clevrKnowledge.pl').
:- include('clevrRules.pl').
:- include('clevrSemanticRules.pl').
:- include('clevrCommonFacts.pl').
find_ans(Q, A) :- question(Q),find_all_filters(ball, 7, L),list_object(L, Ids),get_att_val(Ids, size, A).
question('what is the size of the ball ?').
question_answer('what is the size of the ball ?', A) :- find_ans('what is the size of the ball ?', A).
query(Q, A) :- question(Q),question_answer(Q, A).
