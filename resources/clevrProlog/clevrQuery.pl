:- include('clevrKnowledge.pl').
:- include('clevrRules.pl').
:- include('clevrSemanticRules.pl').
:- include('clevrCommonFacts.pl').
find_ans(Q) :- question(Q),find_all_filters(block, L1),list_object(L1, Ids1),get_att_val(Ids1, size, Val),find_all_filters(object, L2),list_object([[size,Val]|L2], Ids2),list_subtract(Ids2, Ids1, Ids),list_length(Ids, C),quantification(N, object),gte(C, N).
question('is there a nonmetal object of the same size as the blue block ?').
question_answer('is there a nonmetal object of the same size as the blue block ?', true) :- find_ans('is there a nonmetal object of the same size as the blue block ?').
question_answer('is there a nonmetal object of the same size as the blue block ?', false) :- not(find_ans('is there a nonmetal object of the same size as the blue block ?')).
query(Q, A) :- question(Q),question_answer(Q, A).
