#include 'clevrKnowledge.lp'.
#include 'clevrRules.lp'.
#include 'clevrSemanticRules.lp'.
#include 'clevrCommonFacts.lp'.
find_ans(Q) :- question(Q),find_all_filters(ball, L),list_object(L, Ids),list_length(Ids, C),quantification(N, ball),gte(C, N).
question('is there a red ball ?').
question_answer('is there a red ball ?', true) :- find_ans('is there a red ball ?').
question_answer('is there a red ball ?', false) :- not find_ans('is there a red ball ?').
query(Q, A) :- question(Q),question_answer(Q, A).
?- question(Q),find_all_filters(ball, L),list_object(L, Ids),list_length(Ids, C),quantification(N, ball).