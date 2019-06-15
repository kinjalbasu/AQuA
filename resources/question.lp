question('Is there a red block ?', 1).
find_all_filters(block, L),list_object(L, Ids),list_length(Ids, C),quantification(N, block),gte(C, N).
?- query(1,Question,answer( Answer, Confidence_Level)).