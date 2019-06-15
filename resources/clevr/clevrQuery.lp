#include 'clevrKnowledge.lp'.
#include 'clevrRules.lp'.
#include 'clevrSemanticRules.lp'.
#include 'clevrCommonFacts.lp'.
find_ans(Q) :- question(Q),find_all_filters(block, L),list_object(L, Ids),list_length(Ids, C),quantification(N, block),gte(C, N).
question_answer('is there a red block ?', true) :- find_ans('is there a red block ?').
question_answer('is there a red block ?', false) :- not find_ans('is there a red block ?').
query(Q, A) :- question(Q),question_answer(Q, A).
question('is there a red block ?').
?- query(Q,A).