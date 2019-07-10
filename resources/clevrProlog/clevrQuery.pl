:- include('clevrKnowledge.pl').
:- include('clevrRules.pl').
:- include('clevrSemanticRules.pl').
:- include('clevrCommonFacts.pl').
find_ans(Q) :- question(Q),find_all_filters(cylinder, 12, L1),list_object(L1, Ids1),get_att_val(Ids1, color, Val),find_all_filters(anything, 3, L2),list_object([[color,Val]|L2], Ids2),list_subtract(Ids2, Ids1, Ids),list_length(Ids, C),quantification(N, anything_3),gte(C, N).
question('is there anything else of the same color as the tiny cylinder ?').
question_answer('is there anything else of the same color as the tiny cylinder ?', true) :- find_ans('is there anything else of the same color as the tiny cylinder ?').
question_answer('is there anything else of the same color as the tiny cylinder ?', false) :- not(find_ans('is there anything else of the same color as the tiny cylinder ?')).
query(Q, A) :- question(Q),question_answer(Q, A).
