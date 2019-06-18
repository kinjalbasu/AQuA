#include 'clevrKnowledge.lp'.
#include 'clevrRules.lp'.
#include 'clevrSemanticRules.lp'.
#include 'clevrCommonFacts.lp'.

find_ans(Q) :- question(Q),find_all_filters(cylinder, L),list_object(L, Ids),list_length(Ids, C),quantification(N, cylinder),gte(C, N).

question('is there a pink cylinder ?').

question_answer('is there a pink cylinder ?', true) :- find_ans('is there a pink cylinder ?').
question_answer('is there a pink cylinder ?', false) :- not find_ans('is there a pink cylinder ?').

query(Q, A) :- question(Q),question_answer(Q, A).


%?- query(Q,A).


%?- question(Q),find_all_filters(cylinder, L),list_object(L, Ids),list_length(Ids, C),quantification(N, cylinder).



?- question(Q),find_all_filters(cylinder, L),list_length([], C),quantification(N, cylinder),gte(C,N).