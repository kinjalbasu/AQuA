:- include('clevrKnowledge.pl').
:- include('clevrRules.pl').
:- include('clevrSemanticRules.pl').
:- include('clevrCommonFacts.pl').
find_ans(Q) :- question(Q),find_all_filters(ball, L1),find_all_filters(thing, L2),list_object(L1, Ids1),list_object(L2, Ids2),get_att_val(Ids1, size, Val),get_att_val(Ids2, size, Val).
question('is the size of the ball the same as the cyan thing ?').
question_answer('is the size of the ball the same as the cyan thing ?', true) :- find_ans('is the size of the ball the same as the cyan thing ?').
question_answer('is the size of the ball the same as the cyan thing ?', false) :- not(find_ans('is the size of the ball the same as the cyan thing ?')).
query(Q, A) :- question(Q),question_answer(Q, A).
