/*******************************************************************************
 * Copyright (c) 2010 Denis Solonenko.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Denis Solonenko - initial API and implementation
 *     Abdsandryk - adding bill filtering parameters
 ******************************************************************************/
package ru.orangesoftware.financisto2.activity;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;

import ru.orangesoftware.financisto2.R;
import ru.orangesoftware.financisto2.adapter.EntityEnumAdapter;
import ru.orangesoftware.financisto2.model.Account;
import ru.orangesoftware.financisto2.model.AccountType;
import ru.orangesoftware.financisto2.model.CardIssuer;
import ru.orangesoftware.financisto2.model.Currency;
import ru.orangesoftware.financisto2.model.ElectronicPaymentType;
import ru.orangesoftware.financisto2.model.Transaction;
import ru.orangesoftware.financisto2.utils.EntityEnum;
import ru.orangesoftware.financisto2.utils.EnumUtils;
import ru.orangesoftware.financisto2.utils.TransactionUtils;
import ru.orangesoftware.financisto2.utils.Utils;
import ru.orangesoftware.financisto2.widget.AmountInput;
import ru.orangesoftware.financisto2.widget.AmountInput_;

import static ru.orangesoftware.financisto2.utils.EnumUtils.selectEnum;
import static ru.orangesoftware.financisto2.utils.Utils.text;

@EActivity(R.layout.account)
@OptionsMenu(R.menu.account_menu)
public class AccountActivity extends AbstractActivity {

	private static final int NEW_CURRENCY_REQUEST = 1;

    @Extra
    public long accountId = -1;

    private AmountInput amountInput;
	private AmountInput limitInput;
	private View limitAmountView;
	private EditText accountTitle;

	private Cursor currencyCursor;
	private TextView currencyText;
	private View accountTypeNode;
	private View cardIssuerNode;
    private View electronicPaymentNode;
	private View issuerNode;
	private EditText numberText;
	private View numberNode;
	private EditText issuerName;
	private CheckBox isIncludedIntoTotals;
    private EditText noteText;
    private EditText closingDayText;
    private EditText paymentDayText;
    private View closingDayNode;
    private View paymentDayNode;

	private EntityEnumAdapter<AccountType> accountTypeAdapter;
	private EntityEnumAdapter<CardIssuer> cardIssuerAdapter;
    private EntityEnumAdapter<ElectronicPaymentType> electronicPaymentAdapter;
	private ListAdapter currencyAdapter;

	private Account account = new Account();

    @AfterViews
    public void afterViews() {
		accountTitle = new EditText(this);
		accountTitle.setSingleLine();
		
		issuerName = new EditText(this);
		issuerName.setSingleLine();

		numberText = new EditText(this);
		numberText.setHint(R.string.card_number_hint);
		numberText.setSingleLine();

		closingDayText = new EditText(this);
		closingDayText.setInputType(InputType.TYPE_CLASS_NUMBER);
		closingDayText.setHint(R.string.closing_day_hint);
		closingDayText.setSingleLine();
		
		paymentDayText = new EditText(this);
		paymentDayText.setInputType(InputType.TYPE_CLASS_NUMBER);
		paymentDayText.setHint(R.string.payment_day_hint);
		paymentDayText.setSingleLine();

		amountInput = AmountInput_.build(this);
		amountInput.setOwner(this);

		limitInput = AmountInput_.build(this);
		limitInput.setOwner(this);

		LinearLayout layout = (LinearLayout)findViewById(R.id.layout);		

		accountTypeAdapter = EnumUtils.createEntityEnumAdapter(this, AccountType.values());
		accountTypeNode = x.addListNodeIcon(layout, R.id.account_type, R.string.account_type, R.string.account_type);
		
		cardIssuerAdapter = EnumUtils.createEntityEnumAdapter(this, CardIssuer.values());
		cardIssuerNode = x.addListNodeIcon(layout, R.id.card_issuer, R.string.card_issuer, R.string.card_issuer);
		setVisibility(cardIssuerNode, View.GONE);

        electronicPaymentAdapter = EnumUtils.createEntityEnumAdapter(this, ElectronicPaymentType.values());
        electronicPaymentNode = x.addListNodeIcon(layout, R.id.electronic_payment_type, R.string.electronic_payment_type, R.string.card_issuer);
        setVisibility(electronicPaymentNode, View.GONE);

		issuerNode = x.addEditNode(layout, R.string.issuer, issuerName);
		setVisibility(issuerNode, View.GONE);
		
		numberNode = x.addEditNode(layout, R.string.card_number, numberText);
		setVisibility(numberNode, View.GONE);
		
		closingDayNode = x.addEditNode(layout, R.string.closing_day, closingDayText);
		setVisibility(closingDayNode, View.GONE);
		
		paymentDayNode = x.addEditNode(layout, R.string.payment_day, paymentDayText);
		setVisibility(paymentDayNode, View.GONE);		

		currencyCursor = db.getAllCurrencies("name");
		startManagingCursor(currencyCursor);		
		currencyAdapter = TransactionUtils.createCurrencyAdapter(this, currencyCursor);

		x.addEditNode(layout, R.string.title, accountTitle);		
		currencyText = x.addListNodePlus(layout, R.id.currency, R.id.currency_add, R.string.currency, R.string.select_currency);
		
		limitInput.setExpense();
		limitInput.disableIncomeExpenseButton();
		limitAmountView = x.addEditNode(layout, R.string.limit_amount, limitInput);
		setVisibility(limitAmountView, View.GONE);

        if (accountId != -1) {
            this.account = db.getAccount(accountId);
            if (this.account == null) {
                this.account = new Account();
            }
        } else {
            selectAccountType(AccountType.valueOf(account.type));
        }

		if (account.id == -1) {
			x.addEditNode(layout, R.string.opening_amount, amountInput);
            amountInput.setIncome();
		}

        noteText = new EditText(this);
        noteText.setLines(2);
        x.addEditNode(layout, R.string.note, noteText);

		isIncludedIntoTotals = x.addCheckboxNode(layout,
				R.id.is_included_into_totals, R.string.is_included_into_totals,
				R.string.is_included_into_totals_summary, true);
		
		if (account.id > 0) {
			editAccount();
		}

	}

    @OptionsItem(R.id.menu_save)
    public void onSave() {
        if (account.currency == null) {
            Toast.makeText(this, R.string.select_currency, Toast.LENGTH_SHORT).show();
            return;
        }
        if (Utils.isEmpty(accountTitle)) {
            accountTitle.setError(getString(R.string.title));
            return;
        }
        AccountType type = AccountType.valueOf(account.type);
        if (type.hasIssuer) {
            account.issuer = Utils.text(issuerName);
        }
        if (type.hasNumber) {
            account.number = Utils.text(numberText);
        }

        /********** validate closing and payment days **********/
        if (type.isCreditCard) {
            String closingDay = Utils.text(closingDayText);
            account.closingDay = closingDay == null ? 0 : Integer.parseInt(closingDay);
            if (account.closingDay != 0) {
                if (account.closingDay>31) {
                    Toast.makeText(this, R.string.closing_day_error, Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            String paymentDay = Utils.text(paymentDayText);
            account.paymentDay = paymentDay == null ? 0 : Integer.parseInt(paymentDay);
            if (account.paymentDay != 0) {
                if (account.paymentDay>31) {
                    Toast.makeText(this, R.string.payment_day_error, Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        }

        account.title = text(accountTitle);
        account.creationDate = System.currentTimeMillis();
        account.isIncludeIntoTotals  = isIncludedIntoTotals.isChecked();
        account.limitAmount = -Math.abs(limitInput.getAmount());
        account.note = text(noteText);

        long accountId = db.saveAccount(account);
        long amount = amountInput.getAmount();
        if (amount != 0) {
            Transaction t = new Transaction();
            t.fromAccountId = accountId;
            t.categoryId = 0;
            t.note = getResources().getText(R.string.opening_amount) + " (" +account.title + ")";
            t.fromAmount = amount;
            db.insertOrUpdate(t, null);
        }
        Intent intent = new Intent();
        intent.putExtra("accountId", accountId);
        setResult(RESULT_OK, intent);
        finish();
    }

    @OptionsItem(R.id.menu_cancel)
    public void onCancel() {
        setResult(RESULT_CANCELED);
        finish();
    }

    @Override
	protected void onClick(View v, int id) {
		switch(id) {
			case R.id.is_included_into_totals:
				isIncludedIntoTotals.performClick();
				break;
			case R.id.account_type:
				x.selectPosition(this, R.id.account_type, R.string.account_type, accountTypeAdapter, AccountType.valueOf(account.type).ordinal());
				break;
			case R.id.card_issuer:				
				x.selectPosition(this, R.id.card_issuer, R.string.card_issuer, cardIssuerAdapter, 
						selectEnum(CardIssuer.class, account.cardIssuer, CardIssuer.VISA).ordinal());
				break;
            case R.id.electronic_payment_type:
                x.selectPosition(this, R.id.electronic_payment_type, R.string.electronic_payment_type, electronicPaymentAdapter,
                        selectEnum(ElectronicPaymentType.class, account.cardIssuer, ElectronicPaymentType.PAYPAL).ordinal());
                break;
			case R.id.currency:
				x.select(this, R.id.currency, R.string.currency, currencyCursor, currencyAdapter, 
						"_id", account.currency != null ? account.currency.id : -1);
				break;
			case R.id.currency_add:
                addNewCurrency();
				break;
		}
	}

    private void addNewCurrency() {
        new CurrencySelector(this, db, new CurrencySelector.OnCurrencyCreatedListener() {
            @Override
            public void onCreated(long currencyId) {
                if (currencyId == 0) {
                    CurrencyActivity_.intent(AccountActivity.this).startForResult(NEW_CURRENCY_REQUEST);
                } else {
                    currencyCursor.requery();
                    selectCurrency(currencyId);
                }
            }
        }).show();
    }

    @Override
	public void onSelectedId(int id, long selectedId) {
		switch(id) {
			case R.id.currency:
				selectCurrency(selectedId);
				break;
		}
	}

	@Override
	public void onSelectedPos(int id, int selectedPos) {
		switch(id) {
			case R.id.account_type:
				AccountType type = AccountType.values()[selectedPos];
				selectAccountType(type);
				break;
			case R.id.card_issuer:
				CardIssuer issuer = CardIssuer.values()[selectedPos];
				selectCardIssuer(issuer);
				break;
            case R.id.electronic_payment_type:
                ElectronicPaymentType payementType = ElectronicPaymentType.values()[selectedPos];
                selectElectronicType(payementType);
                break;
		}
	}

	private void selectAccountType(AccountType type) {
		ImageView icon = (ImageView)accountTypeNode.findViewById(R.id.icon);
		icon.setImageResource(type.iconId);
		TextView label = (TextView)accountTypeNode.findViewById(R.id.label);
		label.setText(type.titleId);

		setVisibility(cardIssuerNode, type.isCard ? View.VISIBLE : View.GONE);
        setVisibility(electronicPaymentNode, type.isElectronic ? View.VISIBLE : View.GONE);
		setVisibility(issuerNode, type.hasIssuer ? View.VISIBLE : View.GONE);
		setVisibility(numberNode, type.hasNumber ? View.VISIBLE : View.GONE);
		setVisibility(closingDayNode, type.isCreditCard ? View.VISIBLE : View.GONE);
		setVisibility(paymentDayNode, type.isCreditCard ? View.VISIBLE : View.GONE);
		setVisibility(limitAmountView, type == AccountType.CREDIT_CARD ? View.VISIBLE : View.GONE);

		account.type = type.name();
		if (type.isCard) {
            selectCardIssuer(selectEnum(CardIssuer.class, account.cardIssuer, CardIssuer.VISA));
        } else if (type.isElectronic) {
            selectElectronicType(selectEnum(ElectronicPaymentType.class, account.cardIssuer, ElectronicPaymentType.PAYPAL));
        } else {
            account.cardIssuer = null;
        }
	}

	private void selectCardIssuer(CardIssuer issuer) {
        updateNode(cardIssuerNode, issuer);
		account.cardIssuer = issuer.name();
	}

    private void selectElectronicType(ElectronicPaymentType paymentType) {
        updateNode(electronicPaymentNode, paymentType);
        account.cardIssuer = paymentType.name();
    }

    private void updateNode(View note, EntityEnum enumItem) {
        ImageView icon = (ImageView) note.findViewById(R.id.icon);
        icon.setImageResource(enumItem.getIconId());
        TextView label = (TextView) note.findViewById(R.id.label);
        label.setText(enumItem.getTitleId());
    }

	private void selectCurrency(long currencyId) {
        Currency c = db.get(Currency.class, currencyId);
		if (c != null) {
			selectCurrency(c);
		}
	}
	
	private void selectCurrency(Currency c) {
		currencyText.setText(c.name);
		amountInput.setCurrency(c);
		limitInput.setCurrency(c);
		account.currency = c;		
	}

	private void editAccount() {
        AccountType type = AccountType.valueOf(account.type);
        selectAccountType(type);
		selectCurrency(account.currency);
		accountTitle.setText(account.title);
		issuerName.setText(account.issuer);
		numberText.setText(account.number);

		/******** bill filtering ********/
		if (account.closingDay>0) {
			closingDayText.setText(String.valueOf(account.closingDay));
		} 
		if (account.paymentDay>0) {
			paymentDayText.setText(String.valueOf(account.paymentDay));
		}
		/********************************/		
		
		isIncludedIntoTotals.setChecked(account.isIncludeIntoTotals);
		if (account.limitAmount != 0) {
			limitInput.setAmount(-Math.abs(account.limitAmount));
		}
        noteText.setText(account.note);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			if (amountInput.processActivityResult(requestCode, data)) {
				return;
			}
			if (limitInput.processActivityResult(requestCode, data)) {
				return;
			}
			switch(requestCode) {
			case NEW_CURRENCY_REQUEST:
				currencyCursor.requery();
				long currencyId = data.getLongExtra(CurrencyActivity.CURRENCY_ID_EXTRA, -1);
				if (currencyId != -1) {
					selectCurrency(currencyId);
				}
				break;
			}
		}
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}	
		
}
