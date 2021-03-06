/*******************************************************************************
 * Copyright (c) 2010 Denis Solonenko.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Denis Solonenko - initial API and implementation
 ******************************************************************************/
package ru.orangesoftware.financisto2.blotter;

import ru.orangesoftware.financisto2.db.DatabaseHelper.BlotterColumns;

public interface BlotterFilter {

	String FROM_ACCOUNT_ID = BlotterColumns.from_account_id.name();
	String FROM_ACCOUNT_CURRENCY_ID = BlotterColumns.from_account_currency_id.name();
	String CATEGORY_ID = BlotterColumns.category_id.name();
	String CATEGORY_LEFT = BlotterColumns.category_left.name();
	String PROJECT_ID = BlotterColumns.project_id.name();
    String PAYEE_ID = BlotterColumns.payee_id.name();
	String DATETIME = BlotterColumns.datetime.name();
	String BUDGET_ID = "budget_id";
	String IS_TEMPLATE = BlotterColumns.is_template.name();
    String PARENT_ID = BlotterColumns.parent_id.name();
	String STATUS = BlotterColumns.status.name();
	
	String SORT_NEWER_TO_OLDER = BlotterColumns.datetime+" desc";
	String SORT_OLDER_TO_NEWER = BlotterColumns.datetime+" asc";

    String SORT_NEWER_TO_OLDER_BY_ID = "_id desc";
    String SORT_OLDER_TO_NEWER_BY_ID = "_id asc";

}
