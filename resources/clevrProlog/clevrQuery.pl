:- include('clevrKnowledge.pl').
:- include('clevrRules.pl').
:- include('clevrSemanticRules.pl').
:- include('clevrCommonFacts.pl').
find_ans(Q) :- question(Q),find_all_filters(cylinder, 14, L1),list_object(L1, Ids1),get_att_val(Ids1, size, Val),find_all_filters(sphere, 6, L2),list_object([[size,Val]|L2], Ids2),list_subtract(Ids2, Ids1, Ids),list_length(Ids, C),quantification(N, sphere_6),gte(C, N).
question('is there a blue nonmetal sphere that has the same size as the cylinder ?').
question_answer('is there a blue nonmetal sphere that has the same size as the cylinder ?', true) :- find_ans('is there a blue nonmetal sphere that has the same size as the cylinder ?').
question_answer('is there a blue nonmetal sphere that has the same size as the cylinder ?', false) :- not(find_ans('is there a blue nonmetal sphere that has the same size as the cylinder ?')).
query(Q, A) :- question(Q),question_answer(Q, A).
