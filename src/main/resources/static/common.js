let emails;

function runCouponsPromise(couponsPromise){
    couponsPromise.then((successMessage) => {
        emails = JSON.parse(successMessage);
        $('#coupons').DataTable({
            data: emails,
            "columns": [
                {
                    "data": "dateReceived",
                    "width": "100px"
                },
                {
                    "data": "account",
                    "render": function(data, type, row) {
                        return row.account.username;
                    },
                    "width": "100px"
                },
                {
                    "data": "subject",
                    "render": function ( data, type, row, meta ) {
                        return '<a ' + (row.readInd ? '' : 'style="font-weight:bold" ')
                        + 'href=# onclick="openModal(this)" id='+ row.uid + '>' + row.subject + '</a>';
                    }
                }
            ],
            "order": [[ 0, "desc" ]],
            responsive: true,
            "pageLength": 50
        });
    });
}

function runDeletePromise(deletePromise) {
    deletePromise.then((successMessage) => {
        console.log(successMessage);
    })
}